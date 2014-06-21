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

	/* Returns m, the value used to determine the number of samples (s) for
	 * a given confidence alpha in the equation: 
	 *
	 * s = max z²(1/m)(1 - 1/m)/d²
	 *
	 * where m is defined by S. K. Thompson in "Sample Size for Estimating
	 * Multinomial Proportions" to be worst-case in the following ranges of
	 * alpha:
	 *
	 * [0.0000, 0.0344) -> 2
	 * [0.0344, 0.3466) -> 3
	 * [0.3466, 0.6311) -> 4
	 * [0.6311, 0.8934) -> 5
	 * [0.8934, 1.0000) -> 6
	 */
	private static TreeMap<Double, Double> mByAlpha = new TreeMap<Double, Double>();
	static {
		mByAlpha.put(0.0000, 2.0);
		mByAlpha.put(0.0344, 3.0);
		mByAlpha.put(0.3466, 4.0);
		mByAlpha.put(0.6311, 5.0);
		mByAlpha.put(0.8934, 6.0);
	}

	static double getMinSamplesPerCategory(double alpha, double distance) {
		if ((alpha < 0.0) || (alpha >= 1.0) || (distance <= 0.0)) {
			throw new IllegalArgumentException("Alpha must be between [0, 1), and distance must be positive.");
		}

		final double m = mByAlpha.floorEntry(alpha).getValue();

		/* "z is the size of the upper (alpha/2m) * 100th
		 * portion of the standard normal distribution."
		 *
		 * So z = zscore(1.0 - (alpha / (2.0 * m)));
		 */
		final double z = pToZ(1.0 - (alpha / (2.0 * m)));

		return Math.ceil(Math.pow(z, 2.0)*(1.0/m)*(1.0-1.0/m)/Math.pow(distance, 2.0));
	}

	// From http://stats.stackexchange.com/questions/71788/percentile-to-z-score-in-php-or-java
	private static double pToZ(double p) {
	    return Math.sqrt(2) * Erf.erfcInv(2*p);
	}

	public static int[] sort(List<Double> input, int cellSize, double alpha, double cdfDistance) {
		final double inputSize = input.size();
		final double numCells = Math.ceil(inputSize / (double) cellSize);

		// First we'll determine the range of values.
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for (Double value : input) {
			if (value == null) {
				throw new IllegalArgumentException("No value in the array can be null.");
			} else if (value.doubleValue() < min) {
				min = value.doubleValue();
			} else if (value.doubleValue() > max) {
				max = value.doubleValue();
			}
		}

		final double perCellRange = (max - min) / numCells;

		int[] sampleCountsPerCell = new int[(int) numCells];

		/* The next step is to randomly sample elements from the list to
		 * build a cumulative distribution function from.  For simplicity,
		 * we'll choose every (size/sample)th element.
		 */
		final double numSamples = Math.min(getMinSamplesPerCategory(alpha, cdfDistance), inputSize);

		final int randomSampleIndex = (int) Math.floor(inputSize / numSamples);
		for (int index = 0; index < input.size(); index += randomSampleIndex) {
			++sampleCountsPerCell[(int) ((input.get(index) - min) / perCellRange)];
		}

		/* Now that we have a distribution for the set of samples,
		 * we can calculate the cumulative distribution function.
		 */
		double[] cdf = new double[sampleCountsPerCell.length];
		final double sc = numSamples + numCells;
		double prevSi = 0.0;
		for (int cellIndex = 0; cellIndex < sampleCountsPerCell.length; ++cellIndex) {
			cdf[cellIndex] = (sampleCountsPerCell[cellIndex] + 1.0) / sc + prevSi;
			prevSi = cdf[cellIndex];
		}

		/* With the CDF, we can now split the data into buckets,
		 * each containing a near-equal number of elements.
		 */
		for (int i = 0; i < input.size(); ++i) {
			final double value = input.get(i);
			final int cellNum = (int) Math.floor((value - min) / perCellRange);
			final double prevCdf = (cellNum == 0.0) ? 0.0 : cdf[cellNum - 1];
			final double slope = (cdf[cellNum] - prevCdf) / perCellRange;

			/* "The second step finds px, the cumulative probability or CDF of x.
			 *  It equals to the cumulative probability of its preceding cell
			 *  plus the cumulative probability of elements smaller than x in
			 *  this cell.  The latter term is calculated based on the slope
			 *  of the cell.  The calculation assumes a uniform distribution 
			 *  within each cell."
			 *
			 * We want the integral of y = mx + b where b is the cdf of the
			 * previous cell, m is the slope calculated above, and x is the
			 * current value.  Since this is a triangle, we need 0.5 * y * x.
			 *
			 * I think.  It's worth a shot.
			 */
			final double y = slope * value - prevCdf;
			final double currCdf = 0.5 * y * value + prevCdf;

			final int bucketNum = (int) Math.ceil(currCdf * numCells);
			// TODO: Confirm this bucket num is sane.

			/* Next: use the flash sort cycling algorithm
			 *       to bucket these values in-place.
			 */
		}


		return null;
	}
}
