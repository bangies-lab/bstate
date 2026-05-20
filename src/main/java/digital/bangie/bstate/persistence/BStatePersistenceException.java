package digital.bangie.bstate.persistence;

public class BStatePersistenceException extends RuntimeException {
    public BStatePersistenceException(String message) {
        super(message);
    }

    public BStatePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}