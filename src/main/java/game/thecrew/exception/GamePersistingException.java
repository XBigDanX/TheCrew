package game.thecrew.exception;

public class GamePersistingException extends RuntimeException {
    public GamePersistingException(String message, Throwable cause) {
        super(message, cause);
    }
}
