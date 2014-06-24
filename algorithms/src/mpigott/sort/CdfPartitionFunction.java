package mpigott.sort;

import java.util.List;
import java.util.TreeMap;

import org.apache.commons.math3.special.Erf;

/**
 * This is an implementation of the partition function described in
 * "Adaptive Data Partition for Sorting using Probability Distribution"
 * ( http://www.cs.rochester.edu/~cding/Documents/Publications/icpp04.pdf ).
 *
 * The function takes a sample of the input values and generates a cumulative
 * distribution function (CDF) from those values.  It uses that CDF to determine
 * the class that a value in the array belongs in when {@link #getClass(Element)}
 * is called.
 *
 * @author  Mike Pigott
 * @version 1.0
 */
public final class CdfPartitionFunction<T extends Element<U>, U> implements PartitionFunction<T, U> {

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

	/* "We model the problem as a multinomial proportion estimation - a problem
	 *  to find the smallest number, s, of random samples from a multinomial
	 *  population (i.e. a population including multiple categories) such that
	 *  with at least (1 - alpha) probability the estimated distribution is
	 *  within a specified distance of the true population."
	 *
	 * This is calculated using the equation above:
	 *
	 *                     s = max z²(1/m)(1 - 1/m)/d²
	 */
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
		final double z = percentileToZscore(1.0 - (alpha / (2.0 * m)));

		return Math.ceil(Math.pow(z, 2.0)*(1.0/m)*(1.0-1.0/m)/Math.pow(distance, 2.0));
	}

	// From http://stats.stackexchange.com/questions/71788/percentile-to-z-score-in-php-or-java
	private static double percentileToZscore(double percentile) {
	    return Math.sqrt(2) * Erf.erfcInv(2*percentile);
	}

	/**
	 * Generates a cumulative distribution function (CDF) for
	 * <code>Math.ceil(input.size() / cellSize)</code> classes.
	 *
	 * The number of samples to take is defined by the <code>alpha</code>
	 * (where <code>1-alpha</code> is the confidence level) and distance
	 * between the sample CDF and the population CDF. 
	 *
	 * @param input       The population to retrieve samples from.
	 * @param cellSize    The size of each cell.
	 * @param alpha       <code>1 - alpha</code> is the confidence level that the sample CDF is within the <code>cdfDistance</code> of the population CDF.
	 * @param cdfDistance The distance between the sample CDF and the population CDF, with a confidence level of <code>1 - alpha</code>.
	 */
	public CdfPartitionFunction(List<T> input, int cellSize, double alpha, double cdfDistance) {
		final double inputSize = input.size();
		numCells = Math.ceil(inputSize / (double) cellSize);

		// First we'll determine the range of values.
		min = input.get(0);
		T max = input.get(0);

		for (int index = 1; index < input.size(); ++index) {
			T value = input.get(index);
			if (value == null) {
				throw new IllegalArgumentException("No value in the array can be null.");
			} else if (value.compareTo(min) < 0) {
				min = value;
			} else if (value.compareTo(max) > 0) {
				max = value;
			}
		}

		perCellRange = max.distance(min) / numCells;

		int[] sampleCountsPerCell = new int[(int) numCells];

		/* The next step is to randomly sample elements from the list to
		 * build a cumulative distribution function from.  For simplicity,
		 * we'll choose every (size/sample)th element.
		 */
		final double numSamples = Math.min(getMinSamplesPerCategory(alpha, cdfDistance), inputSize);

		final int randomSampleIndex = (int) Math.floor(inputSize / numSamples);
		int actualNumSamples = 0;
		for (int index = 0; index < input.size(); index += randomSampleIndex) {
			int cell = (int) (input.get(index).distance(min) / perCellRange);
			if (cell >= sampleCountsPerCell.length) {
				cell = sampleCountsPerCell.length - 1;
			}
			++sampleCountsPerCell[cell];
			++actualNumSamples;
		}

		/* Now that we have a distribution for the set of samples,
		 * we can calculate the cumulative distribution function.
		 */
		cdf = new double[sampleCountsPerCell.length];
		final double sc = actualNumSamples + numCells;
		double prevSi = 0.0;
		for (int cellIndex = 0; cellIndex < sampleCountsPerCell.length; ++cellIndex) {
			cdf[cellIndex] = (sampleCountsPerCell[cellIndex] + 1.0) / sc + prevSi;
			prevSi = cdf[cellIndex];
		}
	}

	/**
	 * Returns the class of the input <code>element</code>
	 * based on the CDF function of the sample sizes.
	 *
	 * @see PartitionFunction#getClass(Element)
	 */
	public int getClass(T element) {
		double value = element.distance(min);

		int cellNum = (int) (value / perCellRange);
		if (cellNum >= cdf.length) {
			cellNum = cdf.length - 1;
		}

		/* "The second step finds px, the cumulative probability or CDF of x.
		 *  It equals to the cumulative probability of its preceding cell
		 *  plus the cumulative probability of elements smaller than x in
		 *  this cell.  The latter term is calculated based on the slope
		 *  of the cell.  The calculation assumes a uniform distribution 
		 *  within each cell."
		 *
		 * This is implemented as y = mx + b, where b is the cdf of the
		 * previous cell, m is the slope calculated above, and x is the
		 * current value.
		 */
		double prevRange = cellNum * perCellRange;
		double prevCdf = (cellNum == 0) ? 0.0 : cdf[cellNum - 1];

		final double slope = (cdf[cellNum] - prevCdf) / perCellRange;
		final double x = value - prevRange;
		final double px = slope * x + prevCdf;

		double classification = (int)(px * numCells);

		if (classification >= numCells) {
			classification = (numCells - 1.0);
		}

		return (int) classification;
	}

	/**
	 * The total number of classes that an item can be partitioned into.
	 *
	 * @see PartitionFunction#getNumClasses()
	 */
	public int getNumClasses() {
		return (int) numCells;
	}

	private T min;
	private double[] cdf;
	private final double numCells;
	private final double perCellRange;
}
