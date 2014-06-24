Cycle Partitioner
=================
This code is licensed under the Public Domain.

This is an implementation of both the [Flash Sort Partition algorithm](http://www.drdobbs.com/database/the-flashsort1-algorithm/184410496) and the [CDF-Based Partition algorithm](http://www.cs.rochester.edu/~cding/Documents/Publications/icpp04.pdf).  Both are classification-based partitioners, so the partition functions themselves can be separated from the code that performs the partitioning.

The code is broken up into six classes.  Three allow for the partitioner to be abstracted away from the partitioning function:

* `PartitionFunction`: An interface representing a partition function.
* `Element<T>`: An element in the input list.  It extends `Comparable<T>` and computes the relative distance from another `Element<T>`.
* `CyclePartitioner`: This performs the partitioning, given a `List<Element<T>>` and a `PartitionFunction`.  The partitioning is performed in-place, and a list of the upper-bounds of each class is returned.

There are two implementations of `PartitionFunction`:

* `FlashSortPartitionFunction`: The partition function based on the [Flash Sort Partition algorithm](http://www.drdobbs.com/database/the-flashsort1-algorithm/184410496).  This finds the range between the maximum and minimum elements, and divides that range into n equal classes.
* `CdfPartitionFunction`: This is a more-advanced algorithm.  Instead of dividing the [min, max] range equally, this algorithm takes a random sampling of the input and partitions based on the cumulative distribution function generated from those samples.

Finally, there is one implementation of `Element<T>`:

* `NumericElement<T>`: This supports any subclass of `java.lang.Number` which also implements `java.lang.Comparable<T>` (i.e. all of them).  This abstraction allows for more complex data types to be used in the sorting algorithm.

## `CyclePartitioner`

This is an implementation of the algorithm described in the [Flash Sort Partition algorithm](http://www.drdobbs.com/database/the-flashsort1-algorithm/184410496).  When an element in the list is not in its correct class, it becomes the cycle leader.  The cycle leader is moved to a position inside its correct class, evicting the element already there.  That element is then classified and moved, evicting the next element, and so on, until the position the cycle leader was in is filled.  Then we find the next element in the list that is out of place, and start again.

In both implementations of the `PartitionFunction`, all of the classes are expected to contain nearly the same number of elements.  However, some classes can be larger than others, and in those cases, the class sizes must be expanded accordingly.  Classes are always expanded in the direction of the cycle leader, as it is the only known position in the list with an open slot.  So if we expand into an already full neighbor, that neighbor will also expand towards the cycle leader, until the cycle leader is full.

Likewise, the `CyclePartitioner` partitions in `O(N)` time complexity in the best case, with `O(C)` space complexity, where `C` is the number of classes.  In the worst case, all elements need to be moved on all iterations, or `O(N^2)`.

## Results

I have found that larger classes lessen the number of total moves to partition the data.  This makes intuitive sense because the larger the average class size, the less the cascading effect when one class fills up.

I have found that the `CdfPartitionFunction` is only significantly better (in class-size standard deviation and wall-clock performance) than the `FlashSortPartitionFunction` when the data is normally distributed.  Both show similar wall-clock performance, while the `FlashSortPartitionFunction` shows better class-size standard deviation, in evenly-distributed data.

## External Libraries

* This code is implemented in Java 7, but I believe it does not take advantage of any features specific to Java 7.
* The unit tests are written against JUnit 4.
* The `CdfPartitionFunction` and unit tests require [Jakarta Commons Math 3.x](http://commons.apache.org/proper/commons-math/).