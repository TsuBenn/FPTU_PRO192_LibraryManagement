package exceptions;

public class DuplicateEntryException extends LibraryException {
    public DuplicateEntryException(String field, String value) {
        super("Duplicate entry: " + field + " '" + value + "' already exists.");
    }
}
