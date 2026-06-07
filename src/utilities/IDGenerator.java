package utilities;

public class IDGenerator {
    private final String prefix;
    private int iterator = -1;

    public IDGenerator(String prefix, int seed) {
        this.prefix = prefix.toUpperCase();
        iterator = seed;
    }

    public IDGenerator(String prefix) {
        this(prefix, 0);
    }

    public String newID() {
        return String.format("%s%05d",prefix, iterator++);
    }
}
