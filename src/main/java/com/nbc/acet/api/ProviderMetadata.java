package com.nbc.acet.api;

/**
 * Snapshot of provider identity — used in reports and registry.
 */
public record ProviderMetadata(
        String providerId,
        String algorithm,
        AlgorithmFamily algorithmFamily,
        ParameterSet parameterSet) {

    public static ProviderMetadata from(CryptoOperationProvider p) {
        return new ProviderMetadata(
                p.providerId(),
                p.algorithm(),
                p.algorithmFamily(),
                p.parameterSet());
    }
}
