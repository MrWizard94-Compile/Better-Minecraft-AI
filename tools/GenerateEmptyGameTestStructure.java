import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPOutputStream;

/**
 * Generates the minimal compressed NBT structure used by the P0 GameTests.
 */
public final class GenerateEmptyGameTestStructure {
    private static final int DATA_VERSION_1_21_1 = 3955;

    private GenerateEmptyGameTestStructure() {
    }

    /**
     * Writes a deterministic empty structure with the requested positive dimensions.
     *
     * @param args output path, size X, size Y, size Z
     * @throws IOException when the output cannot be written
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            throw new IllegalArgumentException("Usage: <output.nbt> <sizeX> <sizeY> <sizeZ>");
        }

        Path output = Path.of(args[0]).toAbsolutePath().normalize();
        int sizeX = parsePositive(args[1], "sizeX");
        int sizeY = parsePositive(args[2], "sizeY");
        int sizeZ = parsePositive(args[3], "sizeZ");
        Files.createDirectories(output.getParent());
        Path temporary = output.resolveSibling(output.getFileName() + ".tmp");

        try (DataOutputStream data = new DataOutputStream(new BufferedOutputStream(
                new GZIPOutputStream(Files.newOutputStream(temporary))))) {
            data.writeByte(10);
            data.writeUTF("");
            writeInt(data, "DataVersion", DATA_VERSION_1_21_1);
            writeIntList(data, "size", sizeX, sizeY, sizeZ);
            writeAirPalette(data);
            writeEmptyCompoundList(data, "blocks");
            writeEmptyCompoundList(data, "entities");
            data.writeByte(0);
        }

        Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
    }

    private static int parsePositive(String raw, String name) {
        int value = Integer.parseInt(raw);
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private static void writeInt(DataOutputStream data, String name, int value) throws IOException {
        data.writeByte(3);
        data.writeUTF(name);
        data.writeInt(value);
    }

    private static void writeIntList(DataOutputStream data, String name, int... values) throws IOException {
        data.writeByte(9);
        data.writeUTF(name);
        data.writeByte(3);
        data.writeInt(values.length);
        for (int value : values) {
            data.writeInt(value);
        }
    }

    private static void writeAirPalette(DataOutputStream data) throws IOException {
        data.writeByte(9);
        data.writeUTF("palette");
        data.writeByte(10);
        data.writeInt(1);
        data.writeByte(8);
        data.writeUTF("Name");
        data.writeUTF("minecraft:air");
        data.writeByte(0);
    }

    private static void writeEmptyCompoundList(DataOutputStream data, String name) throws IOException {
        data.writeByte(9);
        data.writeUTF(name);
        data.writeByte(10);
        data.writeInt(0);
    }
}
