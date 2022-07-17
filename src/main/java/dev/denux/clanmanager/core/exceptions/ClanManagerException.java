package dev.denux.clanmanager.core.exceptions;

/**
 * Runtime Exception that is thrown for everything that is related to DIH4JDA.
 */
public class ClanManagerException extends RuntimeException {

    public ClanManagerException(String message) {
        super(message);
    }

    public ClanManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClanManagerException(Throwable cause) {
        super(cause);
    }
}
