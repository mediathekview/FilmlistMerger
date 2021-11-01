package de.mediathekview.fimlistmerger;

public class UnknownFilmlistFormatException extends Exception{
    public UnknownFilmlistFormatException() {
        super("A filmlist with an unknown format was read!");
    }
}
