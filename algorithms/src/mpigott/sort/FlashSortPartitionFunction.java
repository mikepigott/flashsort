package mpigott.sort;

import java.util.List;

/**
 * Calculates and returns the class an element belongs in.
 *
 * @author  Mike Pigott
 * @version 1.0
 */
public class FlashSortPartitionFunction<T extends Element<U>, U> {

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
	 */
	public int getClass(T value) {
		double classification =
			Math.floor(numClasses*value.distance(min)/max.distance(min));
		if (classification == numClasses) {
			--classification;
		}

		return (int) classification;
	}

	public int getNumClasses() {
		return (int) numClasses;
	}

	private double numClasses;
	private T min;
	private T max;
}
