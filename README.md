# Database-FileSort

## TEST 결과

### HeapFileBasicTest
```
HeapFile Pages:
Page 0: 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16
Page 1: 17,18,19,20,21,22,23,24,X,X,X,X,X,X,X,X

HeapFile Pages:
Page 0: 1,X,3,X,5,X,7,X,9,X,11,X,13,X,15,X
Page 1: 17,X,19,X,21,X,23,X,X,X,X,X,X,X,X,X

HeapFile Pages:
Page 0: 1,20,3,40,5,60,7,80,9,100,11,120,13,140,15,160
Page 1: 17,180,19,200,21,220,23,240,X,X,X,X,X,X,X,X

Searching for Record with Key 80 in HeapFile:
Found Record Key: 80, Data: HeapData80                                                                                                                                                                                                                                                

Deleting Record with Key 40 in HeapFile:
Record with Key 40 deleted successfully.

HeapFile Pages:
Page 0: 1,20,3,X,5,60,7,80,9,100,11,120,13,140,15,160
Page 1: 17,180,19,200,21,220,23,240,X,X,X,X,X,X,X,X

Performing Range Search in HeapFile from 10 to 50:
  Record Key: 20, Data: HeapData20                                                                                                                                                                                                                                                
  Record Key: 11, Data: HeapData11                                                                                                                                                                                                                                                
  Record Key: 13, Data: HeapData13                                                                                                                                                                                                                                                
  Record Key: 15, Data: HeapData15                                                                                                                                                                                                                                                
  Record Key: 17, Data: HeapData17                                                                                                                                                                                                                                                
  Record Key: 19, Data: HeapData19                                                                                                                                                                                                                                                
  Record Key: 21, Data: HeapData21                                                                                                                                                                                                                                                
  Record Key: 23, Data: HeapData23
```

### SortedFileBasicTest
```
SortedFile Pages:
Page 0: 10,40,50,60,70,90,120,130,160,170,180,200,210,220,230,240
Page 1: 20,30,80,100,110,140,150,190,X,X,X,X,X,X,X,X

SortedFile Pages:
Page 0: 10,50,70,90,130,170,210,230,X,X,X,X,X,X,X,X
Page 1: 30,110,150,190,X,X,X,X,X,X,X,X,X,X,X,X

SortedFile Pages:
Page 0: 10,15,35,50,55,70,75,90,95,115,130,135,155,170,210,230
Page 1: 30,110,150,175,190,195,215,235,X,X,X,X,X,X,X,X

Searching for Record with Key 15 in SortedFile:
Found Record Key: 15, Data: SortedData15                                                                                                                                                                                                                                              

Deleting Record with Key 50 in SortedFile:
Record with Key 50 deleted successfully.

SortedFile Pages:
Page 0: 10,15,35,55,70,75,90,95,115,130,135,155,170,210,230,X
Page 1: 30,110,150,175,190,195,215,235,X,X,X,X,X,X,X,X

Performing Range Search in SortedFile from 40 to 80:
  Record Key: 55, Data: SortedData55                                                                                                                                                                                                                                              
  Record Key: 70, Data: SortedData70                                                                                                                                                                                                                                              
  Record Key: 75, Data: SortedData75                                                                                                                                                                                                                                              
```

### PerformanceTest
```
HeapFile Performance Test:
HeapFile Insert Time: 417.28 ms
Disk Reads: 937
Disk Writes: 2000
HeapFile Search Time for 1000 searches: 7764.51 ms
HeapFile Range Search Time for 1000 ranges: 7581.91 ms
Disk Reads: 123068
Disk Writes: 0

SortedFile Performance Test:
SortedFile Insert Time: 3910.09 ms
Disk Reads: 31689
Disk Writes: 2000
SortedFile Search Time for 1000 searches: 7152.51 ms
SortedFile Range Search Time for 1000 ranges: 7561.93 ms
Disk Reads: 123068
Disk Writes: 0

Performance Comparison Summary:
Insertion Time: SortedFile is 0.11 times faster than HeapFile
Search Time: SortedFile is 1.09 times faster than HeapFile
Range Search Time: SortedFile is 1.00 times faster than HeapFile
```
