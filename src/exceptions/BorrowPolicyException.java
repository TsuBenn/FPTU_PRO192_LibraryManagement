package exceptions;

public class BorrowPolicyException extends LibraryException {
    public BorrowPolicyException(String reason) {
        super("Borrow policy violation: " + reason);
    }
}
