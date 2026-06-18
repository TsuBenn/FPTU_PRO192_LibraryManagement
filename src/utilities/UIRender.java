package utilities;

import java.util.ArrayList;
import java.util.List;
import models.Book;
import models.Member;
import models.BorrowTransaction;

public class UIRender {
    private static final String BORDER_LINE = "==================================================";
    private static final String DIVIDER_LINE = "--------------------------------------------------";

    public static void renderHeader(String title) {
        System.out.println("\n" + BORDER_LINE);
        int paddingSpaces = (BORDER_LINE.length() - title.length()) / 2;
        int totalWidth = paddingSpaces + title.length();
        System.out.printf("%" + totalWidth + "s\n", title.toUpperCase());
        System.out.println(BORDER_LINE);
    }

    public static void renderMenu(String menuTitle, String[] options) {
        renderHeader(menuTitle);
        for (int i = 0; i < options.length; i++) {
            System.out.println("  [" + (i + 1) + "] " + options[i]);
        }
        System.out.println("  [0] Back / Exit");
        System.out.println(DIVIDER_LINE);
    }

    private static void renderContentTable(List<?> contents, String title, String... fields) {
        if (fields == null || fields.length == 0) return;

        renderHeader(title);
        if (contents == null || contents.isEmpty()) {
            renderError("No data records available to map in this matrix view.");
            return;
        }

        int columnCount = fields.length;
        int[] columnWidths = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            columnWidths[i] = fields[i].length();
        }

        List<String[]> processedRows = new ArrayList<>();
        for (Object item : contents) {
            if (item == null) continue;
            String[] tokens = item.toString().split("\\|", -1);
            String[] horizontalRow = new String[columnCount];

            for (int i = 0; i < columnCount; i++) {
                horizontalRow[i] = (i < tokens.length && tokens[i] != null) ? tokens[i].trim() : "";
                columnWidths[i] = Math.max(columnWidths[i], horizontalRow[i].length());
            }
            processedRows.add(horizontalRow);
        }

        String gridBorder = buildGridBorder(columnWidths);

        System.out.println(gridBorder);
        System.out.print("|");
        for (int i = 0; i < columnCount; i++) {
            System.out.printf(" %-" + columnWidths[i] + "s |", fields[i]);
        }
        System.out.println("\n" + gridBorder);

        for (String[] cells : processedRows) {
            System.out.print("|");
            for (int i = 0; i < columnCount; i++) {
                System.out.printf(" %-" + columnWidths[i] + "s |", cells[i]);
            }
            System.out.println();
        }
        System.out.println(gridBorder);
    }

    private static StringBuilder repeat(String base, int repeatTime) {
        StringBuilder stringBuilder = new StringBuilder(base);
        for (int i = 0; i < repeatTime; i++)
            stringBuilder.append(base);

        return stringBuilder;
    }

    private static String buildGridBorder(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append(repeat("-", w + 2).append("+"));
        }
        return sb.toString();
    }

    public static void renderBookContentTable(List<Book> books) {
        renderContentTable(books, "Book Inventory Manifest",
                "ID Key", "Title Name", "Author", "Genre Sub-Class", "Year", "Available", "Total Stock", "Type");
    }

    public static void renderMemberContentTable(List<Member> members) {
        renderContentTable(members, "Membership Registry Directory",
                "User Key", "Legal Identity Name", "Verified Phone", "System Email", "Quota Limit", "Tier");
    }

    public static void renderTransactionTable(List<BorrowTransaction> transactions) {
        renderContentTable(transactions, "Circulation History Ledger",
                "Tx ID", "Member ID", "Book ID", "Borrow Date", "Target Due Date", "Return Date", "Fine Settled");
    }

    public static void renderSuccess(String message) {
        System.out.println("\n[SUCCESS] >>> " + message + "\n");
    }

    public static void renderError(String message) {
        System.err.println("\n[ERROR] !!! " + message + " !!!\n");
    }

    /**
     * ADDED: Explicitly defined method to avoid compilation failure.
     * Handles formatted statement rendering without polluting the controller.
     */
    public static void renderFineStatement(double replacementCost, double latePenalty, double totalBill) {
        renderHeader("Billing Receipt Statement");
        if (replacementCost > 0) {
            System.out.printf("  1. Book Replacement Charge : %,.0f VND\n", replacementCost);
            System.out.printf("  2. Accumulative Late Fine  : %,.0f VND\n", latePenalty);
        } else {
            System.out.printf("  1. Accumulative Late Fine  : %,.0f VND\n", latePenalty);
        }
        System.out.println(DIVIDER_LINE);
        System.out.printf("  TOTAL BALANCE DUE          : %,.0f VND\n", totalBill);
        System.out.println(BORDER_LINE);
    }

    public static void pauseEnter() {
        System.out.print("Press Enter to continue...");
        new java.util.Scanner(System.in).nextLine();
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}