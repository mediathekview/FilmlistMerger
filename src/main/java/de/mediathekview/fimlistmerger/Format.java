package de.mediathekview.fimlistmerger;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Format {
    OLD("old format"),
    UNKNOWN("unknown format");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
