import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Represents a record in a heap or sorted file with a unique key and fixed-length data.
 * Each record consists of an integer key and a fixed-length data string (250 bytes).
 */
public class Record {
    public static final int DATA_SIZE = 250; // Fixed size of data in bytes
    public static final int RECORD_SIZE = Integer.BYTES + DATA_SIZE; // Total record size in bytes
    private int key; // Unique identifier for the record
    private String data; // Fixed-length string (250 bytes)

    // Constructor to initialize a record with a key and data
    public Record(int key, String data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        this.key = key;
        this.data = padOrTrimData(data);
    }

    /**
     * Deserializes a record from a byte array.
     *
     * @param bytes Byte array containing the serialized record.
     * @return A Record object reconstructed from the byte array.
     */
    public static Record fromByteArray(byte[] bytes) {
        if (bytes.length != RECORD_SIZE) {
            throw new IllegalArgumentException("Invalid byte array size");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int key = buffer.getInt(); // Deserialize the key
        byte[] dataBytes = new byte[DATA_SIZE];
        buffer.get(dataBytes); // Deserialize the data
        String data = new String(dataBytes, StandardCharsets.UTF_8).trim();
        return new Record(key, data);
    }

    /**
     * Ensures the data is exactly 250 bytes long by padding or trimming as necessary.
     *
     * @param data The input string to adjust.
     * @return A fixed-length string of 250 bytes.
     */
    private String padOrTrimData(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > DATA_SIZE) {
            // Trim the data to fit within 250 bytes
            return new String(bytes, 0, DATA_SIZE, StandardCharsets.UTF_8);
        } else if (bytes.length < DATA_SIZE) {
            // Pad the data with spaces to make it exactly 250 bytes
            StringBuilder paddedData = new StringBuilder(data);
            while (paddedData.toString().getBytes(StandardCharsets.UTF_8).length < DATA_SIZE) {
                paddedData.append(' ');
            }
            return paddedData.toString();
        } else {
            return data; // Data is already 250 bytes
        }
    }

    // Getter for the key
    public int getKey() {
        return key;
    }

    // Getter for the data
    public String getData() {
        return data;
    }

    // Setter for the data
    public void setData(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        this.data = padOrTrimData(data);
    }

    /**
     * Serializes the record to a byte array.
     *
     * @return Byte array representation of the record.
     */
    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
        buffer.putInt(key); // Serialize the key
        buffer.put(data.getBytes(StandardCharsets.UTF_8)); // Serialize the data
        return buffer.array();
    }
}
