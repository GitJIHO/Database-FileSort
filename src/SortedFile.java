import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SortedFile {
    // Static counters for disk I/O statistics
    private static int diskReadCount = 0;
    private static int diskWriteCount = 0;
    private PageDirectory pageDirectory;
    private String dataFilename;
    private String directoryFilename;

    public SortedFile(String dataFilename, String directoryFilename) throws IOException {
        this.dataFilename = dataFilename;
        this.directoryFilename = directoryFilename;
        this.pageDirectory = readDirectoryFromDisk();
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

    /**
     * Inserts a record into the sorted file.
     * 페이지를 정렬된 상태로 유지하면서 레코드를 삽입한다.
     */
    public void insertRecord(Record record) throws IOException {
        // 기존 페이지들을 순차적으로 확인하여 빈 슬롯을 찾는다
        for (PageInfo pageInfo : pageDirectory.getPages()) {
            Page page = readPageFromDisk(pageInfo.getOffset());
            // 빈 슬롯을 찾고, 그 위치에 레코드를 삽입
            for (int i = 0; i < Page.SLOT_COUNT; i++) {
                if (!page.isSlotUsed(i) || page.getRecord(i).getKey() > record.getKey()) {
                    if (page.isSlotUsed(Page.SLOT_COUNT - 1)) {
                        break;
                    }
                    for (int j = Page.SLOT_COUNT - 1; j > i; j--) {
                        if (page.isSlotUsed(j - 1)) {
                            page.insertRecord(j, page.getRecord(j - 1));
                            page.deleteRecord(j - 1);
                        }
                    }
                    page.insertRecord(i, record);
                    pageInfo.setFreeSlots(pageInfo.getFreeSlots() - 1);

                    // 페이지 내부 레코드를 정렬 (빈 슬롯 이후까지 정렬)
                    sortPageRecords(page); // 추가된 정렬 메서드 호출

                    writePageToDisk(page, pageInfo);
                    writeDirectoryToDisk();
                    return;
                }
            }
        }
        // 새로운 페이지 생성
        Page newPage = new Page();
        newPage.insertRecord(0, record);
        long offset = new File(dataFilename).length();
        PageInfo newPageInfo = new PageInfo(offset, Page.SLOT_COUNT - 1);
        pageDirectory.addPage(newPageInfo);
        writePageToDisk(newPage, newPageInfo);
        writeDirectoryToDisk();
    }

    // 페이지 내 레코드들을 정렬하는 메서드
    private void sortPageRecords(Page page) {
        // 삽입된 레코드들이 들어간 페이지 내에서 레코드들을 정렬 (이진 탐색을 통한 정렬)
        for (int i = 0; i < Page.SLOT_COUNT - 1; i++) {
            for (int j = i + 1; j < Page.SLOT_COUNT; j++) {
                if (page.isSlotUsed(i) && page.isSlotUsed(j) && page.getRecord(i).getKey() > page.getRecord(j).getKey()) {
                    // 두 레코드의 키가 순서대로 정렬되지 않은 경우, 교환
                    Record temp = page.getRecord(i);
                    page.insertRecord(i, page.getRecord(j));
                    page.insertRecord(j, temp);
                }
            }
        }
    }

    /**
     * Searches for a record by its key using binary search inside pages.
     * 페이지 내부에서 이진 탐색을 사용하여 레코드를 검색한다.
     */
    public Record searchRecord(int key) throws IOException {
        // 페이지 레벨에서 이진 탐색을 통해 검색할 페이지를 선택
        int left = 0;
        int right = pageDirectory.getPages().size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            PageInfo pageInfo = pageDirectory.getPages().get(mid);
            Page page = readPageFromDisk(pageInfo.getOffset());

            // 페이지의 첫 번째 키와 마지막 키를 계산하여 검색 범위를 좁힘
            int firstKey = Integer.MAX_VALUE;
            int lastKey = Integer.MIN_VALUE;

            for (int i = 0; i < Page.SLOT_COUNT; i++) {
                if (page.isSlotUsed(i)) {
                    int currentKey = page.getRecord(i).getKey();
                    firstKey = Math.min(firstKey, currentKey);
                    lastKey = Math.max(lastKey, currentKey);
                }
            }

            // 찾고자 하는 키가 현재 페이지의 범위 안에 있으면, 페이지 내 이진 탐색
            if (key >= firstKey && key <= lastKey) {
                int pageLeft = 0;
                int pageRight = Page.SLOT_COUNT - 1;

                // 페이지 내 이진 탐색
                while (pageLeft <= pageRight) {
                    int pageMid = pageLeft + (pageRight - pageLeft) / 2;
                    if (!page.isSlotUsed(pageMid)) {
                        pageRight = pageMid - 1;
                        continue;
                    }

                    Record midRecord = page.getRecord(pageMid);

                    if (midRecord.getKey() == key) {
                        return midRecord;
                    } else if (midRecord.getKey() > key) {
                        pageRight = pageMid - 1;
                    } else {
                        pageLeft = pageMid + 1;
                    }
                }

                // 페이지 내에서 해당 키를 찾을 수 없으면 null 반환
                return null;
            }

            // 키가 현재 페이지의 범위보다 작은 경우, 왼쪽 페이지로 범위 좁히기
            if (key < firstKey) {
                right = mid - 1;
            } else {
                // 키가 현재 페이지의 범위보다 큰 경우, 오른쪽 페이지로 범위 좁히기
                left = mid + 1;
            }
        }

        // 페이지를 모두 검색했는데도 없으면 null 반환
        return null;
    }

    /**
     * Deletes a record by its key and shifts remaining records.
     * 삭제 후, 페이지 내부의 레코드를 이동시켜 빈 슬롯을 메운다.
     */
    public boolean deleteRecord(int key) throws IOException {
        for (PageInfo pageInfo : pageDirectory.getPages()) {
            Page page = readPageFromDisk(pageInfo.getOffset());
            for (int i = 0; i < Page.SLOT_COUNT; i++) {
                if (page.isSlotUsed(i) && page.getRecord(i).getKey() == key) {
                    page.deleteRecord(i);
                    // 레코드 삭제 후, 나머지 레코드를 한 칸씩 당긴다.
                    for (int j = i; j < Page.SLOT_COUNT - 1; j++) {
                        if (page.isSlotUsed(j + 1)) {
                            page.insertRecord(j, page.getRecord(j + 1));
                            page.deleteRecord(j + 1);
                        }
                    }
                    sortPageRecords(page); // 삭제 후 페이지 정렬
                    pageInfo.setFreeSlots(pageInfo.getFreeSlots() + 1);
                    writePageToDisk(page, pageInfo);
                    writeDirectoryToDisk();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Range search using binary search within pages.
     * 범위 검색을 위해 페이지 내에서 이진 탐색을 수행하고 해당 범위에 맞는 레코드를 반환한다.
     */
    public List<Record> rangeSearch(int lowerBound, int upperBound) throws IOException {
        List<Record> result = new ArrayList<>();

        // 페이지를 순차적으로 순회
        for (PageInfo pageInfo : pageDirectory.getPages()) {
            Page page = readPageFromDisk(pageInfo.getOffset());

            // 첫 번째 이진 탐색: lowerBound가 위치한 첫 번째 슬롯을 찾기 위해 검색
            int left = 0;
            int right = Page.SLOT_COUNT - 1;
            int startIdx = -1;

            while (left <= right) {
                int mid = left + (right - left) / 2;
                if (!page.isSlotUsed(mid)) {
                    right = mid - 1;
                    continue;
                }

                Record record = page.getRecord(mid);

                if (record.getKey() >= lowerBound) {
                    right = mid - 1;
                    startIdx = mid; // lowerBound보다 크거나 같은 첫 번째 레코드를 찾음
                } else {
                    left = mid + 1;
                }
            }

            if (startIdx == -1) {
                continue; // 범위에 해당하는 레코드가 없다면 다음 페이지로 이동
            }

            // 두 번째 이진 탐색: upperBound가 위치한 마지막 슬롯을 찾기 위해 검색
            left = startIdx;
            right = Page.SLOT_COUNT - 1;
            int endIdx = -1;

            while (left <= right) {
                int mid = left + (right - left) / 2;
                if (!page.isSlotUsed(mid)) {
                    right = mid - 1;
                    continue;
                }

                Record record = page.getRecord(mid);

                if (record.getKey() <= upperBound) {
                    left = mid + 1;
                    endIdx = mid; // upperBound보다 작거나 같은 마지막 레코드를 찾음
                } else {
                    right = mid - 1;
                }
            }

            // 범위에 해당하는 레코드를 result 리스트에 추가
            if (startIdx != -1 && endIdx != -1) {
                for (int i = startIdx; i <= endIdx; i++) {
                    if (page.isSlotUsed(i)) {
                        Record record = page.getRecord(i);
                        result.add(record);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Prints all pages and their records in the sorted file.
     */
    public void printAllPages() throws IOException {
        System.out.println("\nSortedFile Pages:");
        List<PageInfo> pages = pageDirectory.getPages();
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            PageInfo pageInfo = pages.get(pageIndex);
            Page page = readPageFromDisk(pageInfo.getOffset());
            System.out.print("Page " + pageIndex + ": ");
            page.printAllRecords();
        }
    }

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

    private void writeDirectoryToDisk() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(directoryFilename)) {
            fos.write(pageDirectory.toByteArray());
            diskWriteCount++;
        }
    }

    private Page readPageFromDisk(long offset) throws IOException {
        byte[] bytes = new byte[Page.PAGE_SIZE];
        try (RandomAccessFile raf = new RandomAccessFile(dataFilename, "r")) {
            raf.seek(offset);
            raf.readFully(bytes);
            diskReadCount++;
        }
        return Page.fromByteArray(bytes);
    }

    private void writePageToDisk(Page page, PageInfo pageInfo) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(dataFilename, "rw")) {
            raf.seek(pageInfo.getOffset());
            raf.write(page.toByteArray());
            diskWriteCount++;
        }
    }
}
