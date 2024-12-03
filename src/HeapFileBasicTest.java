import java.io.IOException;
import java.util.List;

/**
 * A basic test class for the `HeapFile` file organization. This test performs various operations on a heap file,
 * such as inserting records, deleting records, searching for records by key, and performing range searches.
 * It ensures the correct functionality and integrity of the `HeapFile` implementation, verifying that records
 * are properly managed, retrieved, and manipulated as expected.
 */
public class HeapFileBasicTest {
    public static void main(String[] args) {
        try {
            // Filenames for HeapFile (data file and directory file)
            String heapDataFilename = "hf_basic_test.dat";
            String heapDirectoryFilename = "hf_basic_test.pd";

            // Clean up existing files for a fresh test environment
            new java.io.File(heapDataFilename).delete();
            new java.io.File(heapDirectoryFilename).delete();

            // --- Initialize HeapFile ---
            HeapFile heapFile = new HeapFile(heapDataFilename, heapDirectoryFilename);

            // --- Insert records into HeapFile ---
            for (int i = 1; i <= 24; i++) {
                String data = "HeapData" + i;
                Record record = new Record(i, data); // Create a new record with a key and data
                heapFile.insertRecord(record); // Insert the record into the heap file
            }

            // --- Print all records in HeapFile ---
            heapFile.printAllPages(); // Display all pages and records in the heap file

            // --- Delete even-numbered records ---
            for (int i = 2; i <= 24; i += 2) {
                heapFile.deleteRecord(i);
            }

            // --- Print all records in HeapFile ---
            heapFile.printAllPages(); // Display all pages and records in the heap file

            // --- Insert new records with modified keys ---
            for (int i = 2; i <= 24; i += 2) {
                int key = i * 10; // Generate new key
                String data = "HeapData" + key;
                Record record = new Record(key, data);
                heapFile.insertRecord(record);
            }

            // --- Print all records in HeapFile ---
            heapFile.printAllPages(); // Display all pages and records in the heap file

            // --- Search for a specific record ---
            int searchKey = 80;
            System.out.println("\nSearching for Record with Key " + searchKey + " in HeapFile:");
            Record foundRecord = heapFile.searchRecord(searchKey); // Search for the record with the given key
            if (foundRecord != null) {
                System.out.println("Found Record Key: " + foundRecord.getKey() + ", Data: " + foundRecord.getData());
            } else {
                System.out.println("Record with Key " + searchKey + " not found.");
            }

            // --- Delete a specific record ---
            int deleteKey = 40;
            System.out.println("\nDeleting Record with Key " + deleteKey + " in HeapFile:");
            boolean deleteResult = heapFile.deleteRecord(deleteKey); // Attempt to delete the record
            if (deleteResult) {
                System.out.println("Record with Key " + deleteKey + " deleted successfully.");
            } else {
                System.out.println("Record with Key " + deleteKey + " not found.");
            }

            // --- Print all records in HeapFile after deletion ---
            heapFile.printAllPages(); // Display all records after performing operations

            // --- Perform a range search ---
            int lowerBound = 10;
            int upperBound = 50;
            System.out.println("\nPerforming Range Search in HeapFile from " + lowerBound + " to " + upperBound + ":");
            List<Record> heapRangeResults = heapFile.rangeSearch(lowerBound, upperBound); // Search records within range
            for (Record record : heapRangeResults) {
                System.out.println("  Record Key: " + record.getKey() + ", Data: " + record.getData());
            }

            // Clean up
            new java.io.File(heapDataFilename).delete();
            new java.io.File(heapDirectoryFilename).delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}