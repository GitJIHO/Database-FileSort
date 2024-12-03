import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HeapFile manages a collection of records stored in pages, providing
 * functionality for insertion, search, deletion, and range-based queries.
 */
public class HeapFile {
    private PageDirectory pageDirectory; // Metadata for all pages
    private String dataFilename; // Path to the data file
    private String directoryFilename; // Path to the page directory file

    // Static counters for disk I/O statistics
    private static int diskReadCount = 0;
    private static int diskWriteCount = 0;

    /**
     * Constructs a HeapFile instance with specified filenames for data and directory.
     *
     * @param dataFilename Path to the data file.
     * @param directoryFilename Path to the directory file.
     * @throws IOException If an I/O error occurs while reading the directory.
     */
    public HeapFile(String dataFilename, String directoryFilename) throws IOException {
        this.dataFilename = dataFilename;
        this.directoryFilename = directoryFilename;
        this.pageDirectory = readDirectoryFromDisk();
    }

    /**
     * Inserts a record into the heap file.
     * Allocates a new page if no free slots are available.
     *
     * @param record The record to insert.
     * @throws IOException If an I/O error occurs during the operation.
     */
    public void insertRecord(Record record) throws IOException {
        for (PageInfo pageInfo : pageDirectory.getPages()) {
            if (pageInfo.getFreeSlots() > 0) {
                Page page = readPageFromDisk(pageInfo.getOffset());
                for (int i = 0; i < Page.SLOT_COUNT; i++) {
                    if (!page.isSlotUsed(i)) {
                        page.insertRecord(i, record);
                        pageInfo.setFreeSlots(pageInfo.getFreeSlots() - 1);
                        writePageToDisk(page, pageInfo);
                        writeDirectoryToDisk();
                        return;
                    }
                }
            }
        }
        Page newPage = new Page();
        newPage.insertRecord(0, record);
        long offset = new File(dataFilename).length();
        PageInfo newPageInfo = new PageInfo(offset, Page.SLOT_COUNT - 1);
        pageDirectory.addPage(newPageInfo);
        writePageToDisk(newPage, newPageInfo);
        writeDirectoryToDisk();
    }

    /**
     * Searches for a record by its key.
     *
     * @param key The key of the record to search for.
     * @return The matching record, or null if not found.
     * @throws IOException If an I/O error occurs during the operation.
     */
    public Record searchRecord(int key) throws IOException {
        for (PageInfo pageInfo : pageDirectory.getPages()) {
            Page page = readPageFromDisk(pageInfo.getOffset());
            for (int i = 0; i < Page.SLOT_COUNT; i++) {
                if (page.isSlotUsed(i) && page.getRecord(i).getKey() == key) {
                    return page.getRecord(i);
                }
            }
        }
        return null; // Record not found
    }

    /**
     * Deletes a record by its key.
     *
     * @param key The key of the record to delete.
     * @return True if the record was successfully deleted, false otherwise.
     * @throws IOException If an I/O error occurs during the operation.
     */
    public boolean deleteRecord(int key) throws IOException {
        for (PageInfo pageInfo : pageDirectory.getPages()) {
            Page page = readPageFromDisk(pageInfo.getOffset());
            for (int i = 0; i < Page.SLOT_COUNT; i++) {
                if (page.isSlotUsed(i) && page.getRecord(i).getKey() == key) {
                    page.deleteRecord(i);
                    pageInfo.setFreeSlots(pageInfo.getFreeSlots() + 1);
                    writePageToDisk(page, pageInfo);
                    writeDirectoryToDisk();
                    return true;
                }
            }
        }
        return false; // Record not found
    }

    /**
     * Performs a range search for records with keys within the specified bounds.
     *
     * @param lowerBound The lower bound of the range (inclusive).
     * @param upperBound The upper bound of the range (inclusive).
     * @return A list of matching records.
     * @throws IOException If an I/O error occurs during the operation.
     */
    public List<Record> rangeSearch(int lowerBound, int upperBound) throws IOException {
        List<Record> result = new ArrayList<>();
        for (PageInfo pageInfo : pageDirectory.getPages()) {
            Page page = readPageFromDisk(pageInfo.getOffset());
            for (int i = 0; i < Page.SLOT_COUNT; i++) {
                if (page.isSlotUsed(i)) {
                    Record record = page.getRecord(i);
                    if (record.getKey() >= lowerBound && record.getKey() <= upperBound) {
                        result.add(record);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Prints all pages and their records in the heap file.
     *
     * @throws IOException If an I/O error occurs during the operation.
     */
    public void printAllPages() throws IOException {
        System.out.println("\nHeapFile Pages:");
        List<PageInfo> pages = pageDirectory.getPages();
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            PageInfo pageInfo = pages.get(pageIndex);
            Page page = readPageFromDisk(pageInfo.getOffset());
            System.out.print("Page " + pageIndex + ": ");
            page.printAllRecords();
        }
    }

    // Helper method to read the page directory from disk
    private PageDirectory readDirectoryFromDisk() throws IOException {
        File dirFile = new File(directoryFilename);
        if (!dirFile.exists()) {
            return new PageDirectory();
        }
        try (FileInputStream fis = new FileInputStream(directoryFilename)) {
            byte[] data = fis.readAllBytes();
            diskReadCount++;
            return PageDirectory.fromByteArray(data);
        } catch (ClassNotFoundException e) {
            throw new IOException("Failed to load page directory", e);
        }
    }

    // Helper method to write the page directory to disk
    private void writeDirectoryToDisk() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(directoryFilename)) {
            fos.write(pageDirectory.toByteArray());
            diskWriteCount++;
        }
    }

    // Helper method to read a page from disk at the specified offset
    private Page readPageFromDisk(long offset) throws IOException {
        byte[] bytes = new byte[Page.PAGE_SIZE];
        try (RandomAccessFile raf = new RandomAccessFile(dataFilename, "r")) {
            raf.seek(offset);
            raf.readFully(bytes);
            diskReadCount++;
        }
        return Page.fromByteArray(bytes);
    }

    // Helper method to write a page to disk
    private void writePageToDisk(Page page, PageInfo pageInfo) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(dataFilename, "rw")) {
            raf.seek(pageInfo.getOffset());
            raf.write(page.toByteArray());
            diskWriteCount++;
        }
    }

    // Methods to access disk I/O statistics
    public static int getDiskReadCount() {
        return diskReadCount;
    }

    public static int getDiskWriteCount() {
        return diskWriteCount;
    }

    public static void resetDiskIOCounters() {
        diskReadCount = 0;
        diskWriteCount = 0;
    }
}