package utilities;

public class IDManager {

    private static int bookCounter = 1;
    private static int memberCounter = 1;
    private static int transactionCounter = 1;

    public static String generateBookID() {
        return String.format("B%03d", bookCounter++); // e.g., B001, B002
    }

    public static String generateMemberID() {
        return String.format("M%03d", memberCounter++); // e.g., M001, M002
    }

    public static String generateTransactionID() {
        return String.format("T%03d", transactionCounter++); // e.g., T001, T002
    }

    // For Milestone 4: Call these when loading files so your counters don't reset!
    public static void updateBookCounter(int lastIdNum) {
        if (lastIdNum >= bookCounter) {
            bookCounter = lastIdNum + 1;
        }
    }

    public static void updateMemberCounter(int lastIdNum) {
        if (lastIdNum >= memberCounter) {
            memberCounter = lastIdNum + 1;
        }
    }
}
