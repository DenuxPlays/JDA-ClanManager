package dev.denux.clanmanager.core.exceptions;

/**
 * Runtime Exception that is thrown for everything that is related to the ClanManager.
 */
public class ClanManagerException extends RuntimeException {

    public ClanManagerException(String message) {
        super(message);
    }

    public ClanManagerException(Throwable cause) {
        super(cause);
    }
}
