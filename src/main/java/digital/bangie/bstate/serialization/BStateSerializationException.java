package digital.bangie.bstate.serialization;

public class BStateSerializationException extends RuntimeException {
    public BStateSerializationException(String message) {
        super(message);
    }

    public BStateSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
