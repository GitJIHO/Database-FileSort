import java.nio.ByteBuffer;

/**
 * Represents a page in a storage system, containing a header to track used slots
 * and an array of records.
 * Each page has a fixed size of 4KB, and can store a fixed number of records (16 in this case).
 * The header uses a bitmap to track which slots are occupied by records.
 */
public class Page {
    private byte[] header; // Bitmap to track used slots
    private Record[] records; // Array to store records in the page

    public static final int PAGE_SIZE = 4096; // 4KB page size
    public static final int RECORD_SIZE = Record.RECORD_SIZE;
    public static final int SLOT_COUNT = 16; // Number of record slots
    public static final int HEADER_SIZE = (int) Math.ceil(SLOT_COUNT / 8.0); // Header size in bytes

    /**
     * Constructs an empty page with initialized header and record array.
     */
    public Page() {
        header = new byte[HEADER_SIZE]; // Initialize the header
        records = new Record[SLOT_COUNT]; // Initialize the record array
    }

    /**
     * Validates that the slot index is within the allowed range.
     *
     * @param slotIndex The slot index to validate.
     * @throws IllegalArgumentException if the slot index is out of range.
     */
    private void validateSlotIndex(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) {
            throw new IllegalArgumentException("Invalid slot index: " + slotIndex);
        }
    }

    /**
     * Checks whether a specific slot is marked as used in the header bitmap.
     *
     * @param slotIndex The index of the slot to check.
     * @return True if the slot is used, false otherwise.
     */
    public boolean isSlotUsed(int slotIndex) {
        validateSlotIndex(slotIndex); // Ensure the slot index is valid
        int byteIndex = slotIndex / 8; // Determine which byte in the header
        int bitIndex = slotIndex % 8; // Determine which bit in the byte
        return (header[byteIndex] & (1 << bitIndex)) != 0; // Check if the bit is set
    }

    /**
     * Marks a specific slot in the bitmap as used or unused.
     *
     * @param slotIndex The index of the slot to mark.
     * @param used True to mark the slot as used, false to mark it as unused.
     */
    public void setSlotUsed(int slotIndex, boolean used) {
        validateSlotIndex(slotIndex); // Ensure the slot index is valid
        int byteIndex = slotIndex / 8; // Determine which byte in the header
        int bitIndex = slotIndex % 8; // Determine which bit in the byte
        if (used) {
            header[byteIndex] |= (1 << bitIndex); // Set the bit to 1
        } else {
            header[byteIndex] &= ~(1 << bitIndex); // Clear the bit to 0
        }
    }

    /**
     * Inserts a record into a specified slot.
     *
     * @param slotIndex The index of the slot to insert the record into.
     * @param record The record to insert.
     * @throws IllegalArgumentException if the slot index is invalid or already used.
     */
    public void insertRecord(int slotIndex, Record record) {
        validateSlotIndex(slotIndex); // Ensure the slot index is valid
        if (isSlotUsed(slotIndex)) {
            throw new IllegalArgumentException("Slot " + slotIndex + " is already used.");
        }
        records[slotIndex] = record; // Insert the record into the specified slot
        setSlotUsed(slotIndex, true); // Mark the slot as used
    }

    /**
     * Deletes a record from a specified slot.
     *
     * @param slotIndex The index of the slot to delete the record from.
     * @throws IllegalArgumentException if the slot index is invalid or unused.
     */
    public void deleteRecord(int slotIndex) {
        validateSlotIndex(slotIndex); // Ensure the slot index is valid
        if (!isSlotUsed(slotIndex)) {
            throw new IllegalArgumentException("Slot " + slotIndex + " is already empty.");
        }
        records[slotIndex] = null; // Remove the record
        setSlotUsed(slotIndex, false); // Mark the slot as unused
    }

    /**
     * Retrieves a record from a specified slot.
     *
     * @param slotIndex The index of the slot to retrieve the record from.
     * @return The record at the specified slot.
     * @throws IllegalArgumentException if the slot index is invalid or unused.
     */
    public Record getRecord(int slotIndex) {
        validateSlotIndex(slotIndex); // Ensure the slot index is valid
        if (!isSlotUsed(slotIndex)) {
            throw new IllegalArgumentException("Slot " + slotIndex + " is empty.");
        }
        return records[slotIndex]; // Return the record
    }

    /**
     * Returns the number of records currently stored in the page.
     *
     * @return The count of used slots in the page.
     */
    public int getNumberOfRecords() {
        int count = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (isSlotUsed(i)) {
                count++;
            }
        }
        return count; // Return the total count of used slots
    }

    /**
     * Prints all keys currently stored in the page.
     * Unused slots are represented by 'X'.
     */
    public void printAllRecords() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (isSlotUsed(i)) {
                result.append(records[i].getKey());
            } else {
                result.append("X");
            }
            if (i < SLOT_COUNT - 1) {
                result.append(",");
            }
        }
        System.out.println(result);
    }

    /**
     * Serializes the entire page, including the header and records, into a byte array for storage.
     *
     * @return A byte array representation of the page.
     */
    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE); // Allocate buffer for serialization

        // Write the header to the buffer
        buffer.put(header);

        // Write each record to the buffer
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (isSlotUsed(i)) {
                buffer.put(records[i].toByteArray()); // Serialize and add the record data
            } else {
                buffer.put(new byte[RECORD_SIZE]); // Fill empty slots with zeros
            }
        }

        return buffer.array(); // Return the serialized byte array
    }

    /**
     * Deserializes a byte array into a Page object, reconstructing the header and records.
     *
     * @param bytes The byte array containing the serialized page data.
     * @return A Page object reconstructed from the byte array.
     * @throws IllegalArgumentException if the byte array size is invalid.
     */
    public static Page fromByteArray(byte[] bytes) {
        if (bytes.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Invalid page size: " + bytes.length);
        }

        Page page = new Page(); // Create a new page instance
        ByteBuffer buffer = ByteBuffer.wrap(bytes); // Wrap the byte array for reading

        // Read the header from the buffer
        buffer.get(page.header);

        // Read each record from the buffer
        for (int i = 0; i < SLOT_COUNT; i++) {
            byte[] recordBytes = new byte[RECORD_SIZE]; // Allocate space for a record
            buffer.get(recordBytes); // Read the record data
            if (page.isSlotUsed(i)) {
                try {
                    page.records[i] = Record.fromByteArray(recordBytes); // Deserialize the record
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize record at slot " + i, e);
                }
            }
        }

        return page; // Return the reconstructed Page object
    }

    /**
     * Retrieves the array of records stored in the page.
     *
     * @return An array of records.
     */
    public Record[] getRecords() {
        return records;
    }

    /**
     * Retrieves the header bitmap of the page.
     *
     * @return A byte array representing the header bitmap.
     */
    public byte[] getHeader() {
        return header;
    }
}
