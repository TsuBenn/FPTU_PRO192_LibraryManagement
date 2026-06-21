package exceptions;

public class EntityNotFoundException extends LibraryException {
    public EntityNotFoundException(String entityType, String id) {
        super(entityType + " with ID '" + id + "' not found.");
    }
}
