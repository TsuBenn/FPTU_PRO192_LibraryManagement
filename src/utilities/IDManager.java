package utilities;

public class IDManager {

    private int iterator;

    public String newID(String prefix) {
        return String.format("%s%05d",prefix, iterator++);
    }

}
