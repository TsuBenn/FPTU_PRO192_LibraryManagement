package utilities;

import java.util.Comparator;
import java.util.List;

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

    public void sync(List<String> idList) {
        if (idList == null)
            return;

        int maxCount = idList.stream()
                .filter(id -> id.startsWith(PREFIX))
                .mapToInt(s -> {
                    try {
                        return Integer.parseInt(s.substring(PREFIX.length()));
                    } catch (RuntimeException re) {
                        return 0;
                    }
                }).max()
                .orElse(0);
        if (maxCount > iterator)
            this.iterator = maxCount + 1;
    }
}