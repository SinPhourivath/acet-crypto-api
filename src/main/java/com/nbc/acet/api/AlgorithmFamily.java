package com.nbc.acet.api;

public enum AlgorithmFamily {
    /** RSA, ECDSA, ML-DSA (FIPS 204), SLH-DSA (FIPS 205) */
    SIGNATURE,

    /** ML-KEM (FIPS 203), X25519MLKEM768 */
    KEM,

    /** ECDH */
    KEY_AGREEMENT
}
