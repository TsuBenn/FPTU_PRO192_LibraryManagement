package utilities;

import java.time.LocalDate;
import java.util.Scanner;

public class InputController {

    private static Scanner sc = new Scanner(System.in);

    private static void clearMess(int messCount) {
            UIRender.pauseEnter();
            UIRender.clearLinesUp(messCount);
    }

    public static String getString(String prompt) {
        String input;
        System.out.print(prompt);
        input = sc.nextLine().trim();
        return input;
    } 

    // Making a version that has a default errorMessage cause why not. Me lazy sometimes
    public static int getInt(String prompt, String errorMessage) {
        while (true) {
            String input;
            System.out.print(prompt);
            input = sc.nextLine().trim();
            if (Validator.isValidInt(input)) {
                return Integer.parseInt(input);
            }
            UIRender.renderError(errorMessage);
            clearMess(7);
        }
    } 

    public static int getInt(String prompt) {
        return getInt(prompt, "Please type a valid whole number");
    } 

    public static double getDouble(String prompt, String errorMessage) {
        while (true) {
            String input;
            System.out.print(prompt);
            input = sc.nextLine().trim();
            if (Validator.isValidDouble(input)) {
                return Double.parseDouble(input);
            }
            UIRender.renderError(errorMessage);
            clearMess(7);
        }
    } 

    public static double getDouble(String prompt) {
        return getDouble(prompt, "Please type a valid real number");
    } 


    // Use LocalDate cause Java has it, don't need to create my own "Date".

    public static LocalDate getDate(String prompt, String errorMessage) {
        System.out.println(prompt);
        while (true) {
            int day = InputController.getInt("Enter day (DD): ");
            int month = InputController.getInt("Enter month (MM): ");
            int year = InputController.getInt("Enter year (YYYY): ");

            if (Validator.isValidDate(day, month, year)) {
                return LocalDate.of(year, month, day);
            }
            UIRender.renderError(errorMessage);
            clearMess(9);
        }
    } 

    public static LocalDate getDate(String prompt) {
        return getDate(prompt, "Inputted date does not exists, please try again");
    } 

}
