package com.nbc.acet.api;

/**
 * Contract all external crypto provider jars must implement.
 *
 * To register your provider:
 *   1. Depend on acet-crypto-api
 *   2. Implement this interface
 *   3. Create META-INF/services/com.nbc.acet.api.CryptoOperationProvider
 *   4. Add your fully qualified class name to that file
 *   5. Build jar and drop into the watch directory
 */
public interface CryptoOperationProvider {

    /** e.g. "BouncyCastle-1.83", "SunJCE-21", "Acme-HSM-2.0" */
    String providerId();

    /** e.g. "RSA", "ML-KEM", "X25519" */
    String algorithm();

    /** Routes to correct benchmark harness and CAVP validator */
    AlgorithmFamily algorithmFamily();

    /** Specific parameter set this provider handles */
    ParameterSet parameterSet();

    // -------------------------------------------------------------------------
    // Key generation — required for ALL providers
    // -------------------------------------------------------------------------

    KeyPairResult generateKeyPair() throws Exception;

    // -------------------------------------------------------------------------
    // SIGNATURE — implement if algorithmFamily() == SIGNATURE
    // -------------------------------------------------------------------------

    default byte[] sign(byte[] message, byte[] privateKey) throws Exception {
        throw new UnsupportedOperationException(
                providerId() + " does not support sign()");
    }

    default boolean verify(byte[] message, byte[] signature,
                           byte[] publicKey) throws Exception {
        throw new UnsupportedOperationException(
                providerId() + " does not support verify()");
    }

    // -------------------------------------------------------------------------
    // KEM — implement if algorithmFamily() == KEM
    // -------------------------------------------------------------------------

    default EncapsulationResult encapsulate(byte[] publicKey) throws Exception {
        throw new UnsupportedOperationException(
                providerId() + " does not support encapsulate()");
    }

    default byte[] decapsulate(byte[] encapsulation,
                               byte[] privateKey) throws Exception {
        throw new UnsupportedOperationException(
                providerId() + " does not support decapsulate()");
    }

    // -------------------------------------------------------------------------
    // KEY_AGREEMENT — implement if algorithmFamily() == KEY_AGREEMENT
    // -------------------------------------------------------------------------

    default byte[] agree(byte[] myPrivateKey, byte[] theirPublicKey) throws Exception {
        throw new UnsupportedOperationException(
                providerId() + " does not support agree()");
    }

    // -------------------------------------------------------------------------
    // DTOs
    // -------------------------------------------------------------------------

    record KeyPairResult(byte[] publicKey, byte[] privateKey) {}

    record EncapsulationResult(byte[] sharedSecret, byte[] encapsulation) {}
}
