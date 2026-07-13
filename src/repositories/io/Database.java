package repositories.io;

import utilities.UIRender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Database <D> {
    private final Writeable<D> writeRule;
    private final Readable<D> readRule;
    private final Path path;


    public Database(String path, Writeable<D> writeRule, Readable<D> readRule) {
        this.readRule = readRule;
        this.writeRule = writeRule;
        this.path = Paths.get(path);
    }


    private void requireExist() throws IOException {
        if (!Files.exists(path))
            Files.createFile(path);
    }

    public List<D> loadContent() {
        try {
            requireExist();
            return Files.readAllLines(path).stream()
                    .map(readRule::read)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            UIRender.renderError("Can not init data!");
        } catch (RuntimeException re) {
            UIRender.renderError("Failed to load content!");
            UIRender.renderError("Detailed log " + re.getMessage());
        }
        return new ArrayList<>();
    }

    public void save(List<D> datas) {
        if (datas == null)
            return;
        try {
            requireExist();
            Files.write(path, datas.stream()
                    .map(writeRule::write).collect(Collectors.toList()));
        } catch (IOException e) {
            UIRender.renderError("Can not write data!");
        } catch (RuntimeException re) {
            UIRender.renderError("Failed to write content!");
            UIRender.renderError("Detailed log " + re.getMessage());
        }
    }

    public void save(Map<String, D> datas) {
        save(new ArrayList<>(datas.values()));
    }

}
