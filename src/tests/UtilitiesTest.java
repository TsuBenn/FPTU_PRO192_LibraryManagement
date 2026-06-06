package tests;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

import utilities.*;

public class UtilitiesTest {

    public static void testUtilities() {

        // --- SCENARIO 1: Test UI Elements & Formatting ---
        UIRender.clearScreen(); // Will clear if supported by terminal
        UIRender.renderHeader("Executing Utilities Test Suite");

        System.out.println("Testing raw Regex definitions via Validator class:");
        System.out.println("  Is 'abc' a valid int? " + Validator.isValidInt("abc"));
        System.out.println("  Is '125' a valid int? " + Validator.isValidInt("125"));
        System.out.println("  Is '-4.25' a valid double? " + Validator.isValidDouble("-4.25"));
        System.out.println("  Is '0987654321' a valid phone? " + Validator.isValidPhone("0987654321"));

        UIRender.pauseEnter(); // Tests the pause mechanic

        // --- SCENARIO 2: Interactive Menu Display ---
        UIRender.clearScreen();
        String[] mockMainMenuOptions = {
            "Book Inventory Subsystem", 
            "Member Directory Subsystem", 
            "Circulation Transactions", 
            "Analytical Reports"
        };
        // Rendering the menu flexibly
        UIRender.renderMenu("Library Controller Simulation", mockMainMenuOptions); 
        System.out.println("Visual look check complete.");

        UIRender.pauseEnter();

        // --- SCENARIO 3: Testing Guarded Primitive Inputs ---
        UIRender.clearScreen();
        UIRender.renderHeader("Defensive Input Capture Testing");
        System.out.println("NOTE: Please intentionally type bad values (letters, spaces, empty values)");
        System.out.println("to witness the loop trap them without crashing the application!\n");

        // Test the customized string reader
        String sampleName = Input.getString("Enter a tester name: ");
        UIRender.renderSuccess("Captured Name: " + sampleName);

        // Test standard number trap
        int sampleAge = Input.getInt("Enter an age (Whole Number): ");
        UIRender.renderSuccess("Captured Age: " + sampleAge);

        // Test dynamic overloaded error messaging
        double samplePrice = Input.getDouble("Enter target item price: ", "CUSTOM ERROR: Invalid cash amount format!");
        UIRender.renderSuccess("Captured Price: $" + samplePrice);

        UIRender.pauseEnter();

        // --- SCENARIO 4: Calendar Math Verification ---
        UIRender.clearScreen();
        UIRender.renderHeader("Calendar Logic Boundary Testing");
        System.out.println("Try testing invalid dates (e.g., 30/02/2026 or 32/01/2026):");

        // Triggers the nested loop capturing day, month, and year
        LocalDate validatedDate = Input.getDate("Provide a safe target date configuration:");
        UIRender.renderSuccess("Constructed LocalDate Instance: " + validatedDate);

        UIRender.pauseEnter();

        // --- SCENARIO 5: Dynamic Table Formatting Engine ---
        UIRender.clearScreen();
        UIRender.renderHeader("Final Comprehensive Table Render Report");

        String[] headers = {"Asset ID", "Resource Title", "Primary Contributor", "Stock Status"};
        List<String[]> dataRows = new ArrayList<>();

        // Testing columns scaling to accommodate different string lengths dynamically
        dataRows.add(new String[]{"B001", "Dune", "Frank Herbert", "5 Units"});
        dataRows.add(new String[]{"B002", "The Hitchhiker's Guide to the Galaxy", "Douglas Adams", "12 Units"});
        dataRows.add(new String[]{"B003", "Neuromancer", "William Gibson", "0 Units"});
        dataRows.add(new String[]{"M042", "Alice Vance", "Premium Tier", "Active"});

        UIRender.renderTable(headers, dataRows); // Output dynamic dataset

        System.out.println("\nAll utility targets checked successfully.");

    }

}
