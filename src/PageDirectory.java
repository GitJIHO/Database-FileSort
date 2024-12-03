import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PageDirectory is a class that manages a collection of PageInfo objects,
 * providing functionality to add, update, and serialize page information.
 */
public class PageDirectory {
    private List<PageInfo> pages; // List to store page metadata

    /**
     * Constructs an empty PageDirectory.
     */
    public PageDirectory() {
        pages = new ArrayList<>();
    }

    /**
     * Adds a new page to the directory.
     *
     * @param pageInfo The PageInfo object representing the page to add.
     */
    public void addPage(PageInfo pageInfo) {
        pages.add(pageInfo);
    }

    /**
     * Retrieves the list of pages in the directory.
     *
     * @return A list of PageInfo objects.
     */
    public List<PageInfo> getPages() {
        return pages;
    }

    /**
     * Updates the metadata of an existing page in the directory.
     *
     * @param pageInfo The updated PageInfo object.
     */
    public void updatePageInfo(PageInfo pageInfo) {
        // Find and update the page info based on its offset
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getOffset() == pageInfo.getOffset()) {
                pages.set(i, pageInfo); // Update the existing page metadata
                return;
            }
        }
    }

    /**
     * Serializes the PageDirectory into a byte array.
     *
     * @return A byte array representation of the PageDirectory.
     * @throws IOException If an I/O error occurs during serialization.
     */
    public byte[] toByteArray() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(pages); // Serialize the list of PageInfo objects
            return baos.toByteArray();
        }
    }

    /**
     * Deserializes a PageDirectory from a byte array.
     *
     * @param data The byte array containing the serialized PageDirectory.
     * @return A PageDirectory object reconstructed from the byte array.
     * @throws IOException If an I/O error occurs during deserialization.
     * @throws ClassNotFoundException If the serialized object class is not found.
     */
    public static PageDirectory fromByteArray(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            @SuppressWarnings("unchecked")
            List<PageInfo> pages = (List<PageInfo>) ois.readObject();
            PageDirectory pageDirectory = new PageDirectory();
            pageDirectory.pages = pages;
            return pageDirectory;
        }
    }
}
