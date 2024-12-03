import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * PageInfo represents metadata for a page, including its offset in the file
 * and the number of free slots available.
 */
public class PageInfo implements Serializable {
    private long offset;     // Starting offset of the page in the data file
    private int freeSlots;   // Number of free slots in the page

    /**
     * Constructs a PageInfo object with the specified offset and free slot count.
     *
     * @param offset The starting offset of the page in the file.
     * @param freeSlots The number of free slots in the page.
     */
    public PageInfo(long offset, int freeSlots) {
        this.offset = offset;
        this.freeSlots = freeSlots;
    }

    /**
     * Retrieves the offset of the page.
     *
     * @return The starting offset of the page in the file.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Retrieves the number of free slots in the page.
     *
     * @return The number of free slots.
     */
    public int getFreeSlots() {
        return freeSlots;
    }

    /**
     * Updates the number of free slots in the page.
     *
     * @param freeSlots The updated number of free slots.
     */
    public void setFreeSlots(int freeSlots) {
        this.freeSlots = freeSlots;
    }

    /**
     * Serializes the PageInfo object to a byte array.
     *
     * @return A byte array representation of the PageInfo.
     */
    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + Integer.BYTES); // Allocate buffer for offset and freeSlots
        buffer.putLong(offset); // Add offset to the buffer
        buffer.putInt(freeSlots); // Add freeSlots to the buffer
        return buffer.array(); // Return serialized data
    }

    /**
     * Deserializes a byte array to create a PageInfo object.
     *
     * @param bytes A byte array containing the serialized PageInfo data.
     * @return A PageInfo object reconstructed from the byte array.
     */
    public static PageInfo fromByteArray(byte[] bytes) {
        if (bytes.length != Long.BYTES + Integer.BYTES) {
            throw new IllegalArgumentException("Invalid byte array length for PageInfo.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes); // Wrap byte array for reading
        long offset = buffer.getLong(); // Extract offset
        int freeSlots = buffer.getInt(); // Extract freeSlots
        return new PageInfo(offset, freeSlots); // Create and return PageInfo
    }
}
