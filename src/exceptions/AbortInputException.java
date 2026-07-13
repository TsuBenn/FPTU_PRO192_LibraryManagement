package exceptions;

public class AbortInputException extends RuntimeException {
    public AbortInputException() {
        super("Input sequence aborted by user.");
    }
}
