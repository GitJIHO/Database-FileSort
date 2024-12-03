import java.io.IOException;
import java.util.List;

/**
 * A basic test class for the `SortedFile` file organization. This test performs various operations on a sorted file,
 * including inserting records, deleting records, searching for records by key, and performing range searches.
 * It helps verify the correct functionality and integrity of the `SortedFile` implementation, ensuring that records
 * are correctly sorted, managed, retrieved, and manipulated as expected.
 */
public class SortedFileBasicTest {
    public static void main(String[] args) {
        try {
            // Filenames for SortedFile (data file and metadata file)
            String sortedDataFilename = "sf_basic_test.dat";
            String sortedDirectoryFilename = "sf_basic_test.pd";

            // Clean up any existing files to ensure a fresh test environment
            new java.io.File(sortedDataFilename).delete();
            new java.io.File(sortedDirectoryFilename).delete();

            // --- Initialize SortedFile ---
            SortedFile sortedFile = new SortedFile(sortedDataFilename, sortedDirectoryFilename);

            // --- Insert records into SortedFile ---
            // Sample test keys to insert into the sorted file
            int[] testKeys = {120, 40, 200, 90, 160, 230, 170, 240, 50, 130, 10, 220, 180, 60, 210, 70, 150, 80, 110, 30, 190, 20, 100, 140};
            for (int i : testKeys) {
                String data = "SortedData" + i;
                Record record = new Record(i, data);
                sortedFile.insertRecord(record);
            }

            // Print all records in the SortedFile after insertion
            sortedFile.printAllPages();

            // --- Delete records with specific keys ---
            // Delete records with keys in the range of 20 to 240, incremented by 20
            for (int i = 20; i <= 240; i += 20) {
                sortedFile.deleteRecord(i); // Attempt to delete the record
            }

            // Print all records in the SortedFile after deletion
            sortedFile.printAllPages();

            // --- Insert new records with modified keys ---
            // Insert new records with updated keys (by subtracting 5 from the original key values)
            for (int i = 20; i <= 240; i += 20) {
                int key = i - 5;
                String data = "SortedData" + key;
                Record record = new Record(key, data);
                sortedFile.insertRecord(record);
            }

            // Print all records in the SortedFile after new insertions
            sortedFile.printAllPages();

            // --- Search for a specific record ---
            int searchKey = 15; // Define the key to search for
            System.out.println("\nSearching for Record with Key " + searchKey + " in SortedFile:");
            Record foundRecord = sortedFile.searchRecord(searchKey); // Search for the record with the given key
            if (foundRecord != null) {
                System.out.println("Found Record Key: " + foundRecord.getKey() + ", Data: " + foundRecord.getData());
            } else {
                System.out.println("Record with Key " + searchKey + " not found.");
            }

            // --- Delete a specific record ---
            int deleteKey = 50; // Define the key to delete
            System.out.println("\nDeleting Record with Key " + deleteKey + " in SortedFile:");
            boolean deleteResult = sortedFile.deleteRecord(deleteKey); // Attempt to delete the record
            if (deleteResult) {
                System.out.println("Record with Key " + deleteKey + " deleted successfully.");
            } else {
                System.out.println("Record with Key " + deleteKey + " not found.");
            }

            // Print all records in the SortedFile after delete operation
            sortedFile.printAllPages();

            // --- Perform a range search ---
            int lowerBound = 40;
            int upperBound = 80;
            System.out.println("\nPerforming Range Search in SortedFile from " + lowerBound + " to " + upperBound + ":");
            List<Record> sortedRangeResults = sortedFile.rangeSearch(lowerBound, upperBound); // Search for records within the specified range
            for (Record record : sortedRangeResults) {
                System.out.println("  Record Key: " + record.getKey() + ", Data: " + record.getData());
            }

            // Clean up
            new java.io.File(sortedDataFilename).delete();
            new java.io.File(sortedDirectoryFilename).delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
