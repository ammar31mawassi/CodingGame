package com.codeescape.engine;

public record MedalContractProgress(
        boolean satisfied,
        boolean stillPossible,
        String status
) {
    public MedalContractProgress {
        status = status == null ? "" : status;
    }
}
