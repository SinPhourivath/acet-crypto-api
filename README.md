

# acet-crypto-api

Contract API for the **Automated Cryptography Evaluation Tool (ACET)**.

External teams implement this contract to have their cryptographic provider
automatically benchmarked and validated against NIST test vectors when their
jar is dropped into the ACET watch directory.

---

## Requirements
- Java 21+
- Maven 3.8+

---

## Step 1 - Install the contract jar

You will receive `acet-crypto-api-0.0.1-SNAPSHOT.jar` from the ACET team.
Install it to your local Maven repository:
```bash
mvn install:install-file \
  -Dfile=acet-crypto-api-0.0.1-SNAPSHOT.jar \
  -DgroupId=com.nbc.acet \
  -DartifactId=acet-crypto-api \
  -Dversion=0.0.1-SNAPSHOT \
  -Dpackaging=jar
```

## Step 2 - Add the dependency

In your provider project's `pom.xml`:
```xml
<dependency>
	<groupId>com.nbc.acet</groupId>
	<artifactId>acet-crypto-api</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```

> **Important:** Always use `provided` scope. Do NOT shade or bundle
> `acet-crypto-api` into your jar — ACET needs to load its own copy
> at runtime for type compatibility.

## Step 3 - Implement the contract

Implement `CryptoOperationProvider` for each algorithm your provider supports.
One class per parameter set is recommended.

### Algorithm families

| `algorithmFamily()` | Operations to implement | Examples |
|---|---|---|
| `SIGNATURE` | `generateKeyPair()`, `sign()`, `verify()` | RSA, ECDSA, ML-DSA |
| `KEM` | `generateKeyPair()`, `encapsulate()`, `decapsulate()` | ML-KEM, X25519MLKEM768 |
| `KEY_AGREEMENT` | `generateKeyPair()`, `agree()` | ECDH |

### Example - RSA 2048 provider
```java
import com.nbc.acet.api.*;
import java.security.*;
import java.security.spec.*;

public class MyRsa2048Provider implements CryptoOperationProvider {

    @Override
    public String providerId() { return "MyOrg-1.0"; }

    @Override
    public String algorithm() { return "RSA"; }

    @Override
    public AlgorithmFamily algorithmFamily() { return AlgorithmFamily.SIGNATURE; }

    @Override
    public ParameterSet parameterSet() { return ParameterSet.RSA_2048; }

    @Override
    public KeyPairResult generateKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        return new KeyPairResult(
            kp.getPublic().getEncoded(),
            kp.getPrivate().getEncoded());
    }

    @Override
    public byte[] sign(byte[] message, byte[] privateKey) throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey pk = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(pk);
        sig.update(message);
        return sig.sign();
    }

    @Override
    public boolean verify(byte[] message, byte[] signature,
                          byte[] publicKey) throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pk = kf.generatePublic(new X509EncodedKeySpec(publicKey));
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(pk);
        sig.update(message);
        try { return sig.verify(signature); }
        catch (SignatureException e) { return false; }
    }
}
```

### Multiple parameter sets in one jar

One jar can expose multiple providers — one class per parameter set:
```java
// MyRsa2048Provider.java  → parameterSet() = RSA_2048
// MyRsa3072Provider.java  → parameterSet() = RSA_3072
// MyMlKem768Provider.java → parameterSet() = ML_KEM_768
```

## Step 4 - Register via SPI

Create the file:
```
src/main/resources/META-INF/services/com.nbc.acet.api.CryptoOperationProvider
```

List all your provider classes, one per line:
```
com.yourorg.crypto.MyRsa2048Provider
com.yourorg.crypto.MyRsa3072Provider
com.yourorg.crypto.MyMlKem768Provider
```

> The file name must be exactly `com.nbc.acet.api.CryptoOperationProvider`.
> The class names inside are yours - use whatever package you want.

## Step 5 - Build the jar

Your `pom.xml` should shade your dependencies (except `acet-crypto-api`)
into a single self-contained jar
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.2</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
            <configuration>
              <shadedArtifactAttached>false</shadedArtifactAttached>
              <artifactSet>
                <excludes>
                  <exclude>com.nbc.acet:acet-crypto-api</exclude>
                </excludes>
              </artifactSet>
              <filters>
                <filter>
                  <artifact>*:*</artifact>
                  <excludes>
                    <exclude>META-INF/*.SF</exclude>
                    <exclude>META-INF/*.DSA</exclude>
                    <exclude>META-INF/*.RSA</exclude>
                  </excludes>
                </filter>
              </filters>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Build:
```bash
mvn clean package
```

## Step 6 — Drop the jar

Copy your built jar into the ACET watch directory:
```
acet-app/providers/your-provider.jar
```

ACET will automatically detect it, load your providers, run benchmarks,
and validate against NIST test vectors. Check ACET logs for confirmation:
```
INFO: Loading provider jar: your-provider.jar
INFO: Registered: providerId=MyOrg-1.0 algorithm=RSA parameterSet=RSA_2048
INFO: Registered: providerId=MyOrg-1.0 algorithm=ML-KEM parameterSet=ML_KEM_768
```

## Available parameter sets

| Family | ParameterSet values |
|---|---|
| RSA | `RSA_2048`, `RSA_3072`, `RSA_4096` |
| ECDSA | `ECDSA_P256`, `ECDSA_P384` |
| ECDH | `ECDH_P256`, `ECDH_P384` |
| Key Agreement | `X25519` |
| ML-KEM (FIPS 203) | `ML_KEM_512`, `ML_KEM_768`, `ML_KEM_1024` |
| ML-DSA (FIPS 204) | `ML_DSA_44`, `ML_DSA_65`, `ML_DSA_87` |
| SLH-DSA (FIPS 205) | `SLH_DSA_SHA2_128S/F`, `SLH_DSA_SHA2_192S/F`, `SLH_DSA_SHA2_256S/F`, `SLH_DSA_SHAKE_128S/F`, `SLH_DSA_SHAKE_192S/F`, `SLH_DSA_SHAKE_256S/F` |
| Hybrid | `X25519MLKEM768` |


## Key encoding

All keys are passed as the output of Java's standard `getEncoded()` method:

- **Public keys** — `publicKey.getEncoded()`
- **Private keys** — `privateKey.getEncoded()`

To reconstruct them on your side:
```java
// Reconstruct public key
PublicKey pub = KeyFactory.getInstance("RSA")
    .generatePublic(new X509EncodedKeySpec(publicKeyBytes));

// Reconstruct private key
PrivateKey priv = KeyFactory.getInstance("RSA")
    .generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
```

When returning keys from `generateKeyPair()`, use `getEncoded()`:
```java
KeyPair kp = keyPairGenerator.generateKeyPair();
return new KeyPairResult(
    kp.getPublic().getEncoded(),
    kp.getPrivate().getEncoded());
```

## Questions

Contact the ACET team for support.
