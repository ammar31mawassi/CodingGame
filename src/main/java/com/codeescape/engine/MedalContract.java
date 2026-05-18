package com.codeescape.engine;

public record MedalContract(
        MedalContractType type,
        String title,
        String description
) {
    public MedalContract {
        if (type == null) {
            throw new IllegalArgumentException("Contract type is required.");
        }
        title = title == null ? "" : title;
        description = description == null ? "" : description;
    }
}
