package utilities;

import java.util.List;

public class UIRender {

    // Still don't know whether to make UIRender static or not.

    private static final String BORDER_LINE = "==================================================";
    private static final String DIVIDER_LINE = "--------------------------------------------------";

    public static void renderHeader(String title) {
        System.out.println("\n" + BORDER_LINE);

        int paddingSpaces = (BORDER_LINE.length() - title.length()) / 2;
        int totalWidth = paddingSpaces + title.length();
        System.out.printf("%" + totalWidth + "s\n", title.toUpperCase());

        System.out.println(BORDER_LINE);
    }

    /*

        Usage:

        String[] mainMenuOptions = {
            "Book Management", 
            "Member Management", 
            "Transactions", 
            "Reports"
        };

        UIRender.renderMenu("Library Main Menu", mainMenuOptions);

     */
    public static void renderMenu(String menuTitle, String[] options) {
        renderHeader(menuTitle);

        for (int i = 0; i < options.length; i++) {
            System.out.println("  [" + (i + 1) + "] " + options[i]);
        }

        System.out.println("  [0] Back / Exit");
        System.out.println(DIVIDER_LINE);
    }

    /*

        How to use this method:

        String[] headers = {"ID", "Name", "Age", "Class"};

        List<String[]> dataRows = new ArrayList<>();

        dataRows.add(new String[] {"01", "Nguyen Van A", "18", "SE1967"});
        dataRows.add(new String[] {"02", "Nguyen Van B", "18", "SE1969"});
        dataRows.add(new String[] {"03", "Nguyen Van C", "18", "SE6767"});

        UIRender.renderTable(headers, dataRows);

     */
    public static void renderTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) return;

        int[] colWidths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
        }

        // Automatically expand the collumn's width if the content is too long
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (i < colWidths.length && row[i] != null) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
        }

        StringBuilder dividerSB = new StringBuilder("+"); // StringBuilder for mutable String
        for (int width : colWidths) {
            for (int i = 0; i < width + 2; i++) {
                dividerSB.append("-");
            }
            dividerSB.append("+");
        }
        String divider = dividerSB.toString();

        // Print header row
        System.out.println(divider);
        System.out.print("|");
        for (int i = 0; i < headers.length; i++) {
            System.out.printf(" %-" + colWidths[i] + "s |", headers[i]);
        }
        System.out.println("\n" + divider);

        // Print content rows
        for (String[] row : rows) {
            System.out.print("|");
            for (int i = 0; i < headers.length; i++) {
                String cell = (i < row.length && row[i] != null) ? row[i] : "";
                System.out.printf(" %-" + colWidths[i] + "s |", cell);
            }
            System.out.println();
        }

        System.out.println(divider);
    }

    public static void renderSuccess(String message) {
        System.out.println("\n[SUCCESS] >>> " + message + "\n");
    }

    public static void renderError(String message) {
        System.err.println("\n[ERROR] !!! " + message + " !!!\n");
    }

    public static void pauseEnter() {
        Input.getString("\nPress Enter to continue..."); 
    }

    public static void pauseEnter(String prompt) {
        Input.getString("\n" + prompt); 
        System.out.println("");
    }

    // Both of these won't work in NetBeans but work if you run it in the terminal.
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void clearLinesUp(int count) {
        for (int i = 0; i < count; i++) {
            // \033[1A : Move cursor up 1 line
            // \033[2K : Clear that entire line
            System.out.print("\033[1A\033[2K");
        }
        System.out.flush();
    }

}
