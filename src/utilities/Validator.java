package utilities;

import java.time.YearMonth;
import java.util.regex.Pattern;

public class Validator {

    private static final Pattern INT_PATTERN    = Pattern.compile("^[+-]?[0-9]+$");
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("^[+-]?[0-9]+(\\.[0-9]+)?$");
    private static final Pattern EMAIL_PATTERN  = Pattern.compile(
            "^[0-9a-zA-Z._%+-]+@[0-9a-zA-Z.-]+\\.[A-Za-z]{2,6}$");
    private static final Pattern PHONE_PATTERN  = Pattern.compile("^[0-9]{10}$");

    private Validator() {}

    public static boolean isValidInt(String str) {
        return str != null && INT_PATTERN.matcher(str).matches();
    }

    public static boolean isValidDouble(String str) {
        return str != null && DOUBLE_PATTERN.matcher(str).matches();
    }

    public static boolean isValidEmail(String str) {
        return str != null && EMAIL_PATTERN.matcher(str).matches();
    }

    public static boolean isValidPhone(String str) {
        return str != null && PHONE_PATTERN.matcher(str).matches();
    }

    public static boolean isValidDate(int d, int m, int y) {
        if (y < 1 || y > 9999 || m < 1 || m > 12) return false;
        int daysInMonth = YearMonth.of(y, m).lengthOfMonth();
        return d > 0 && d <= daysInMonth;
    }
}
