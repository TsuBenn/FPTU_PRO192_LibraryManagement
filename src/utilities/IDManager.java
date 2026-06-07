package utilities;

public class IDManager {
    private final String PREFIX;
    private final int BASE_GENERATOR_SIZE;
    private int iterator;

    public static final IDManager memberIDGenerator = new IDManager("MEM");
    public static final IDManager bookIDGenerator = new IDManager("BOK");
    public static final IDManager transactionIDGenerator = new IDManager("TSC", 5, 0);

    public IDManager(String prefix, int baseGeneratorSize, int seed) {
        if (prefix == null)
            PREFIX = "NUL";
        else
            this.PREFIX = prefix.toUpperCase();
        this.BASE_GENERATOR_SIZE = baseGeneratorSize;
        this.iterator = seed;
    }

    public IDManager(String prefix) {
        this(prefix, 4, 0);
    }

    public String newID() {
        return String.format("%s%0" + BASE_GENERATOR_SIZE + "d", PREFIX, iterator++);
    }
}