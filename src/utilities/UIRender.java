package utilities;

import java.util.ArrayList;
import java.util.List;
import models.Book;
import models.Member;

public class UIRender {
    private static final String BORDER_LINE = "==================================================";
    public static final String DIVIDER_LINE = "--------------------------------------------------";

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

    public static <T> void renderTable(List<T> items, String title, TableRenderer<T> renderer) {
        renderHeader(title);
        if (items == null || items.isEmpty()) {
            renderError("No data available.");
            return;
        }
        String[] headers = renderer.getHeaders();
        int colCount = headers.length;
        int[] widths = new int[colCount];
        for (int i = 0; i < colCount; i++) widths[i] = headers[i].length();

        List<String[]> rows = new ArrayList<>();
        for (T item : items) {
            String[] row = renderer.toRow(item);
            rows.add(row);
            for (int i = 0; i < colCount; i++)
                if (i < row.length) widths[i] = Math.max(widths[i], row[i].length());
        }

        String border = buildGridBorder(widths);
        System.out.println(border);
        printRow(headers, widths);
        System.out.println(border);
        for (String[] row : rows) printRow(row, widths);
        System.out.println(border);
    }

    private static void printRow(String[] cells, int[] widths) {
        System.out.print("|");
        for (int i = 0; i < widths.length; i++)
            System.out.printf(" %-" + widths[i] + "s |", i < cells.length ? cells[i] : "");
        System.out.println();
    }

    private static String buildGridBorder(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            for (int i = 0; i < w + 2; i++) sb.append("-");
            sb.append("+");
        }
        return sb.toString();
    }

    public static void renderBookPreview(Book b) {
        renderHeader("Review Book Information");
        System.out.println("  Title  : " + b.getTitle());
        System.out.println("  Author : " + b.getAuthor());
        System.out.println("  Genre  : " + b.getGenre());
        System.out.println("  Year   : " + b.getPublicationYear());
        System.out.println("  Stock  : " + b.getTotalQuantity());
        System.out.println("  Type   : " + b.getBookType());
        System.out.println(DIVIDER_LINE);
    }

    public static void renderMemberPreview(Member m) {
        renderHeader("Review Member Information");
        System.out.println("  Name   : " + m.getName());
        System.out.println("  Phone  : " + m.getPhone());
        System.out.println("  Email  : " + m.getEmail());
        System.out.println("  Tier   : " + m.getTierName());
        System.out.println(DIVIDER_LINE);
    }

    public static void renderSuccess(String message) {
        System.out.println("\n[SUCCESS] >>> " + message + "\n");
    }

    public static void renderError(String message) {
        System.err.println("\n[ERROR] !!! " + message + " !!!\n");
    }

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
