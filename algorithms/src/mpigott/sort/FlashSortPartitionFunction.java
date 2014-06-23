package mpigott.sort;

import java.util.List;

/**
 * Implementation of the Flash Sort partition function.
 * http://www.drdobbs.com/database/the-flashsort1-algorithm/184410496
 *
 * The key innovation behind flash sort is that if you have an evenly-distributed
 * data set, you can create "classes" of sub-ranges of equal size - knowing only
 * the global maximum and minimum - and place each element into their appropriate
 * class in O(N) time, in-place.  The algorithm is not stable.
 *
 * @author  Mike Pigott
 * @version 1.0
 */
public class FlashSortPartitionFunction<T extends Element<U>, U> implements PartitionFunction<T, U> {

	/**
	 * The number of classes is an upper bound.  If the range between the global
	 * minimum and maximum is larger than the number of classes, only (max - min + 1)
	 * classes will be used.
	 * 
	 * @param input               The input to generate the classes from.
	 * @param numClassesRequested The maximum number of classes to sort the input into.
	 *
	 */
	public FlashSortPartitionFunction(List<T> input, int numClassesRequested) {
		min = input.get(0);
		max = input.get(0);

		for (int index = 1; index < input.size(); ++index) {
			T value = input.get(index);

			if (value == null) {
				throw new IllegalArgumentException("Input list cannot contain null elements.  The element at index " + index + " is null.");
			}

			if (value.compareTo(min) < 0) {
				min = value;
			} else if (value.compareTo(max) > 0) {
				max = value;
			}
		}

		numClasses = numClassesRequested;
		if ((max.distance(min)) < numClasses) {
			numClasses = Math.floor(max.distance(min) + 1.0);
		}

	}

	/**
	 * Performs the classification.  This is slightly different from the algorithm
	 * in the original Dr. Dobbs article, but in limited testing I found the standard
	 * deviation on the resulting class sizes to be smaller than when using the original.
	 *
	 * @see PartitionFunction#getClass(Element)
	 */
	public int getClass(T value) {
		double classification =
			Math.floor(numClasses*value.distance(min)/max.distance(min));
		if (classification == numClasses) {
			--classification;
		}

		return (int) classification;
	}

	/**
	 * The total number of classes that an item can be partitioned into.
	 *
	 * @see PartitionFunction#getNumClasses()
	 */
	public int getNumClasses() {
		return (int) numClasses;
	}

	private double numClasses;
	private T min;
	private T max;
}
