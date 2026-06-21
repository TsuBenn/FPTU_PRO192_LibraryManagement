package exceptions;

public class InvalidBookException extends LibraryException{
    public InvalidBookException(String message) {
        super(message);
    }
}
