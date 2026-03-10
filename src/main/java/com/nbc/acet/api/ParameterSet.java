package com.nbc.acet.api;

public enum ParameterSet {

    // RSA
    RSA_2048, RSA_3072, RSA_4096,

    // ECDSA
    ECDSA_P256, ECDSA_P384,

    // ECDH
    ECDH_P256, ECDH_P384,

    // Key Agreement
    X25519,

    // FIPS 203 — ML-KEM
    ML_KEM_512, ML_KEM_768, ML_KEM_1024,

    // FIPS 204 — ML-DSA
    ML_DSA_44, ML_DSA_65, ML_DSA_87,

    // FIPS 205 — SLH-DSA
    SLH_DSA_SHA2_128S, SLH_DSA_SHA2_128F,
    SLH_DSA_SHA2_192S, SLH_DSA_SHA2_192F,
    SLH_DSA_SHA2_256S, SLH_DSA_SHA2_256F,
    SLH_DSA_SHAKE_128S, SLH_DSA_SHAKE_128F,
    SLH_DSA_SHAKE_192S, SLH_DSA_SHAKE_192F,
    SLH_DSA_SHAKE_256S, SLH_DSA_SHAKE_256F,

    // Hybrid
    X25519MLKEM768
}
