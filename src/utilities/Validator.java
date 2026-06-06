package utilities;

import java.time.YearMonth;

public class Validator {

    // Haven't learned Try, Catch yet so... Regex :D

    public static boolean isValidInt(String str) {
        return str != null && str.matches("^[+-]?[0-9]+$");
    }

    public static boolean isValidDouble(String str) {
        return str != null && str.matches("^[+-]?[0-9]+(\\.[0-9]+)?$");
    }

    public static boolean isValidEmail(String str) {
        return str != null && str.matches("^[0-9a-zA-Z._%+-]+@[0-9a-zA-Z.-]+\\.[A-Za-z]{2,6}$"); // Assuming an email has text@text.text
    }


    // LocalDate would throw exceptions so we use YearMonth instead
    public static boolean isValidDate(int d, int m, int y) {
        if (y < 1 || y > 9999 || m < 1 || m > 12) {
            return false;
        }

        YearMonth yearMonthObj = YearMonth.of(y, m);
        int daysInMonth = yearMonthObj.lengthOfMonth();

        return d > 0 && d <= daysInMonth;
    }

    public static boolean isValidPhone(String str) {
        return str != null && str.matches("^[0-9]{10}$"); // Assuming a valid phone number is 10 digits number
    }

    // We could validate Book or Member here but I think the BookManager and MemberManager or Book and Member itself should handle it.
    
}
