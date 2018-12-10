package net.gjerull.etherpad.client;

/**
 * The Class EPLiteException.
 */
public class EPLiteException extends RuntimeException {

    /**
     * Instantiates a new EP lite exception.
     *
     * @param message the message
     */
    public EPLiteException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new EP lite exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public EPLiteException(final String message,
            final Throwable cause) {
        super(message, cause);
    }
}
