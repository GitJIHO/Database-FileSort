import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A performance test class for comparing the performance of `HeapFile` and `SortedFile` file organizations.
 * This test measures the time taken for various operations, including inserting records, searching records by key,
 * and performing range searches. The results will help evaluate and compare the efficiency of both file organizations
 * under different types of operations, giving insights into their relative performance strengths and weaknesses.
 */
public class PerformanceTest {
    public static void main(String[] args) {
        try {
            // Define filenames for HeapFile and SortedFile (data and directory files)
            String heapDataFilename = "hf_perf_test.dat";
            String heapDirectoryFilename = "hf_perf_test.pd";
            String sortedDataFilename = "sf_perf_test.dat";
            String sortedDirectoryFilename = "sf_perf_test.pd";

            // Clean up any existing files to ensure a fresh test environment
            new java.io.File(heapDataFilename).delete();
            new java.io.File(heapDirectoryFilename).delete();
            new java.io.File(sortedDataFilename).delete();
            new java.io.File(sortedDirectoryFilename).delete();

            // Number of records to insert, searches to perform, and range searches
            int numRecords = 1000;
            int numSearches = 1000; // Number of searches to perform
            int rangeSearches = 1000; // Number of range searches to perform

            // Generate random keys for insertion (random key range for testing)
            Random rand = new Random();
            List<Integer> keys = new ArrayList<>();
            for (int i = 0; i < numRecords; i++) {
                keys.add(rand.nextInt(numRecords * 10)); // Generate random keys within a wide range
            }

            // Shuffle the keys to simulate random insertion order
            java.util.Collections.shuffle(keys);

            // Generate random keys for searches
            List<Integer> searchKeys = new ArrayList<>();
            for (int i = 0; i < numSearches; i++) {
                searchKeys.add(rand.nextInt(numRecords * 10)); // Random keys for search
            }

            // Generate random ranges for range searches
            List<int[]> ranges = new ArrayList<>();
            for (int i = 0; i < rangeSearches; i++) {
                int lower = rand.nextInt(numRecords * 10); // Random lower bound
                int upper = lower + rand.nextInt(1000); // Range width up to 1000
                ranges.add(new int[]{lower, upper});
            }

            // --- HeapFile Performance Test ---
            System.out.println("HeapFile Performance Test:");
            HeapFile heapFile = new HeapFile(heapDataFilename, heapDirectoryFilename);

            // Measure insertion time for HeapFile
            long heapInsertStart = System.nanoTime();
            for (int key : keys) {
                String data = "HeapData" + key;
                Record record = new Record(key, data);
                heapFile.insertRecord(record); // Insert record into HeapFile
            }
            long heapInsertEnd = System.nanoTime();
            double heapInsertTime = (heapInsertEnd - heapInsertStart) / 1e6; // Convert to milliseconds
            System.out.printf("HeapFile Insert Time: %.2f ms\n", heapInsertTime);
            System.out.println("Disk Reads: " + HeapFile.getDiskReadCount());
            System.out.println("Disk Writes: " + HeapFile.getDiskWriteCount());

            // Reset disk I/O counters for further tests
            HeapFile.resetDiskIOCounters();

            // Measure search time for HeapFile
            long heapSearchStart = System.nanoTime();
            for (int key : searchKeys) {
                heapFile.searchRecord(key); // Search for records by key
            }
            long heapSearchEnd = System.nanoTime();
            double heapSearchTime = (heapSearchEnd - heapSearchStart) / 1e6;
            System.out.printf("HeapFile Search Time for %d searches: %.2f ms\n", numSearches, heapSearchTime);

            // Measure range search time for HeapFile
            long heapRangeSearchStart = System.nanoTime();
            for (int[] range : ranges) {
                heapFile.rangeSearch(range[0], range[1]); // Perform range search
            }
            long heapRangeSearchEnd = System.nanoTime();
            double heapRangeSearchTime = (heapRangeSearchEnd - heapRangeSearchStart) / 1e6;
            System.out.printf("HeapFile Range Search Time for %d ranges: %.2f ms\n", rangeSearches, heapRangeSearchTime);
            System.out.println("Disk Reads: " + HeapFile.getDiskReadCount());
            System.out.println("Disk Writes: " + HeapFile.getDiskWriteCount());

            // --- SortedFile Performance Test ---
            System.out.println("\nSortedFile Performance Test:");
            SortedFile sortedFile = new SortedFile(sortedDataFilename, sortedDirectoryFilename);

            // Measure insertion time for SortedFile
            long sortedInsertStart = System.nanoTime();
            for (int key : keys) {
                String data = "SortedData" + key;
                Record record = new Record(key, data);
                sortedFile.insertRecord(record); // Insert record into SortedFile
            }
            long sortedInsertEnd = System.nanoTime();
            double sortedInsertTime = (sortedInsertEnd - sortedInsertStart) / 1e6;
            System.out.printf("SortedFile Insert Time: %.2f ms\n", sortedInsertTime);
            System.out.println("Disk Reads: " + SortedFile.getDiskReadCount());
            System.out.println("Disk Writes: " + SortedFile.getDiskWriteCount());

            // Reset disk I/O counters for further tests
            SortedFile.resetDiskIOCounters();

            // Measure search time for SortedFile
            long sortedSearchStart = System.nanoTime();
            for (int key : searchKeys) {
                sortedFile.searchRecord(key); // Search for records by key
            }
            long sortedSearchEnd = System.nanoTime();
            double sortedSearchTime = (sortedSearchEnd - sortedSearchStart) / 1e6;
            System.out.printf("SortedFile Search Time for %d searches: %.2f ms\n", numSearches, sortedSearchTime);

            // Measure range search time for SortedFile
            long sortedRangeSearchStart = System.nanoTime();
            for (int[] range : ranges) {
                sortedFile.rangeSearch(range[0], range[1]); // Perform range search
            }
            long sortedRangeSearchEnd = System.nanoTime();
            double sortedRangeSearchTime = (sortedRangeSearchEnd - sortedRangeSearchStart) / 1e6;
            System.out.printf("SortedFile Range Search Time for %d ranges: %.2f ms\n", rangeSearches, sortedRangeSearchTime);
            System.out.println("Disk Reads: " + SortedFile.getDiskReadCount());
            System.out.println("Disk Writes: " + SortedFile.getDiskWriteCount());

            // --- Performance Comparison Summary ---
            System.out.println("\nPerformance Comparison Summary:");

            // Insertion time comparison
            double insertSpeedup = heapInsertTime / sortedInsertTime;
            System.out.printf("Insertion Time: SortedFile is %.2f times faster than HeapFile\n", insertSpeedup);

            // Search time comparison
            double searchSpeedup = heapSearchTime / sortedSearchTime;
            System.out.printf("Search Time: SortedFile is %.2f times faster than HeapFile\n", searchSpeedup);

            // Range search time comparison
            double rangeSearchSpeedup = heapRangeSearchTime / sortedRangeSearchTime;
            System.out.printf("Range Search Time: SortedFile is %.2f times faster than HeapFile\n", rangeSearchSpeedup);

            // Clean up
            new java.io.File(heapDataFilename).delete();
            new java.io.File(heapDirectoryFilename).delete();
            new java.io.File(sortedDataFilename).delete();
            new java.io.File(sortedDirectoryFilename).delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
