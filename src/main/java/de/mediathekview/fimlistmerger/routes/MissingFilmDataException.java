package de.mediathekview.fimlistmerger.routes;

public class MissingFilmDataException extends RuntimeException {

    public MissingFilmDataException(String fieldName, String invalidData) {
        super("Can't create a valid film because \"" + invalidData + "\" isn't valid for " + fieldName);
    }

    public MissingFilmDataException(String fieldName, String invalidData, Throwable cause) {
        super("Can't create a valid film because \"" + invalidData + "\" isn't valid for " + fieldName, cause);
    }


    public MissingFilmDataException(String missingFieldName) {
        super("Can't create a valid film because " + missingFieldName + " is missing.");
    }

}
