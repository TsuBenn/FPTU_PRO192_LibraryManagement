package exceptions;

public class InvalidOperationException extends LibraryException {
    public InvalidOperationException(String reason) {
        super("Operation not allowed: " + reason);
    }
}
