package utilities;

public interface TableRenderer<T> {
    String[] getHeaders();
    String[] toRow(T item);
}
