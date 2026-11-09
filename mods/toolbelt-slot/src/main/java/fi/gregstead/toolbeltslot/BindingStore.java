package fi.gregstead.toolbeltslot;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Client-wide preference. Disk values are 1-9; 0 disables the binding. */
public final class BindingStore {
    private final Path path;
    private int slot = -1;

    public BindingStore(Path path) { this.path = path; }
    public int slot() { return slot; }

    public void load() throws IOException {
        if (!Files.exists(path)) return;
        var properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) { properties.load(reader); }
        try {
            int value = Integer.parseInt(properties.getProperty("hotbarSlot", "0"));
            if (value < 0 || value > 9) throw new NumberFormatException("outside 0-9");
            slot = value - 1;
        } catch (IllegalArgumentException error) {
            slot = -1;
            throw new IOException("Invalid toolbelt hotbarSlot (expected 0-9)", error);
        }
    }

    public void bind(int selected) throws IOException {
        if (selected < -1 || selected > 8) throw new IllegalArgumentException("Invalid hotbar slot");
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path temporary = Files.createTempFile(path.toAbsolutePath().getParent(), "toolbelt-slot-", ".tmp");
        try {
            Files.writeString(temporary, "# 0 = unbound; 1-9 = hotbar position\nhotbarSlot=" + (selected + 1) + "\n");
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            slot = selected;
        } finally { Files.deleteIfExists(temporary); }
    }
}
