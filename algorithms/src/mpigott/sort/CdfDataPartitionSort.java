package mpigott.sort;

import java.util.List;
import java.util.TreeMap;

import org.apache.commons.math3.special.Erf;

/**
 * This implements the sorting algorithm described in
 * http://www.cs.rochester.edu/~cding/Documents/Publications/icpp04.pdf
 * which uses the cumulative distribution function to allocate class sizes.
 *
 * Like Flash Sort and other classification sorting algorithms, this does
 * not sort the data itself.  This only classifies the data into buckets,
 * and moves each data item into its respective bucket.
 *
 * Like Flash Sort, the algorithm is O(N) and unstable.  Unlike Flash Sort,
 * this algorithm uses a cumulative distribution function (CDF) to estimate
 * each class size, and likewise minimizes re-balancing when the data does
 * not fit an equal distribution.
 *
 * The original algorithm uses O(N) additional memory, copying each element
 * into its correct location in a new array.  This implementation will use
 * the constructs for maintaining classes in Flash Sort, allowing the sort
 * to occur in-place.
 *
 * June 20 - ?, 2014.
 *
 * @author  Mike Pigott
 * @version 1.0
 */
public class CdfDataPartitionSort {

	public static <T extends Element<U>, U> int[] sort(List<T> input, int cellSize, double alpha, double cdfDistance) {
		CdfPartitionFunction<T, U> function = new CdfPartitionFunction<T, U>(input, cellSize, alpha, cdfDistance);

		for (int i = 0; i < input.size(); ++i) {
			T value = input.get(i);
			final int bucketNum = function.getClass(value);
			// TODO: Confirm this bucket num is sane.

			/* Next: use the flash sort cycling algorithm
			 *       to bucket these values in-place.
			 */
		}


		return null;
	}
}
