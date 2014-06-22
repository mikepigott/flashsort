package mpigott.sort;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

public class CdfDataPartitionSortTest {

	@Test
	public void testGetMinSamplesPerCategory() {
		assertEquals(12736, (int) CdfPartitionFunction.getMinSamplesPerCategory(0.05, 0.01));
		assertEquals(40612, (int) CdfPartitionFunction.getMinSamplesPerCategory(0.05, 0.0056));
	}

	@Test
	public void testPartitionFunction() {
		ArrayList<NumericElement<Double>> input = createNonRandomInput(10, -4.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
				new CdfPartitionFunction<NumericElement<Double>, Double>(input, 2, 0.9, 0.1);

		SummaryStatistics statistics = getBucketStatistics(input, func);

		assertEquals(5, (int) statistics.getN());
		assertEquals(2, (int) statistics.getMax());
		assertEquals(2, (int) statistics.getMin());
		assertEquals(2, (int) statistics.getMean());
		assertEquals(0, (int) statistics.getStandardDeviation());
	}

	@Test
	public void testPartitionFunction2() {
		ArrayList<NumericElement<Double>> input = createNonRandomInput(100000, -50.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 100, 0.05, 0.01);

		SummaryStatistics statistics = getBucketStatistics(input, func);

		assertEquals(1000, (int) statistics.getN());
		assertTrue(statistics.getStandardDeviation() < 3.0);
		assertEquals(95,   (int) statistics.getMin());
		assertEquals(102,  (int) statistics.getMax());
	}

	@Test
	public void testPartitionFunction3() {
		ArrayList<NumericElement<Double>> input = createNonRandomInput(30000, -50.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 100, 0.05, 0.01);

		SummaryStatistics statistics = getBucketStatistics(input, func);

		assertEquals(300, statistics.getN());
		assertTrue(statistics.getStandardDeviation() < 1.0);
		assertEquals(99, (int) statistics.getMin());
		assertEquals(101, (int) statistics.getMax());
	}

	@Test
	public void testPartitionFunction4() {
		ArrayList<NumericElement<Double>> input = createNonRandomInput(10000, -50.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 100, 0.05, 0.01);

		SummaryStatistics statistics = getBucketStatistics(input, func);

		assertEquals(100, statistics.getN());
		assertTrue(statistics.getStandardDeviation() < 1.0);
		assertEquals(99, (int) statistics.getMin());
		assertEquals(101, (int) statistics.getMax());
	}

	@Test
	public void testPartitioning1() {
		ArrayList<NumericElement<Double>> input = createNonRandomInput(10000, -50.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 100, 0.05, 0.01);

		int[] classBounds = CyclePartitioner.partition(input, func);

		checkInput(input, classBounds);
	}

	@Test
	public void testPartitioning2() {
		ArrayList<NumericElement<Double>> input = createNonRandomInput(100000, -50.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 100, 0.05, 0.01);

		int[] classBounds = CyclePartitioner.partition(input, func);

		// TODO: Fix checkInput(input, classBounds);
		int prevBounds = 0;
		SummaryStatistics statistics = new SummaryStatistics();
		for (int i = 0; i < classBounds.length; ++i) {
			statistics.addValue(classBounds[i] - prevBounds);
			prevBounds = classBounds[i];
		}
		System.out.println(statistics);
	}


	private ArrayList<NumericElement<Double>> createNonRandomInput(int numElements, double min) {
		ArrayList<NumericElement<Double>> input =
				new ArrayList<NumericElement<Double>>(numElements);

		for (int i = 0; i < numElements; ++i) {
			input.add(new NumericElement<Double>(min + i));
		}

		return input;
	}

	private <T extends Element<U>, U> SummaryStatistics getBucketStatistics(ArrayList<T> input, CdfPartitionFunction<T, U> func) {
		TreeMap<Integer, Double> bucketSizes = new TreeMap<Integer, Double>();
		for (int i = 0; i < input.size(); ++i) {
			Integer bucket = func.getClass(input.get(i));
			Double count = bucketSizes.get(bucket);
			if (count == null) {
				count = 0.0;
			}
			bucketSizes.put(bucket, count + 1.0);
		}

		SummaryStatistics statistics = new SummaryStatistics();

		for (Map.Entry<Integer, Double> bucket : bucketSizes.entrySet()) {
			statistics.addValue(bucket.getValue());
		}

		return statistics;
	}

	private void checkInput(ArrayList<NumericElement<Double>> output, int[] classBounds) {
		assertNotNull(classBounds);

		// Confirm the max value of a class is less than the min value of the next upper class.
		double[] prevMinAndMax = getMinAndMax(output, classBounds, 0);
		double[] currMinAndMax = null;

		for (int i = 1; i < classBounds.length; ++i) {
			currMinAndMax = getMinAndMax(output, classBounds, i);
			final boolean isValid = prevMinAndMax[1] < currMinAndMax[0];
			if (!isValid) {
			}
			assertTrue("Maximum of class " + (i - 1) + " (" + prevMinAndMax[1] + ") must be less than the min of class " + i + " (" + currMinAndMax[0] + ").", isValid);
			prevMinAndMax = currMinAndMax;
		}

		// Confirm the ranges are valid.
		for (int i = 1; i < classBounds.length; ++i) {
			final int range = (classBounds[i] - classBounds[i - 1]);
			if (range < 0) {
				int startIndex = (i == 1) ? 0 : classBounds[i - 2] + 1;
				int endIndex = classBounds[i - 1];
				for (int indexInRange = startIndex; indexInRange < endIndex; ++indexInRange) {
					if (indexInRange == classBounds[i]) {
						System.out.print("[*] ");
					}
					System.out.print("[" + indexInRange + ": " + output.get(indexInRange) + "], ");
				}
				System.out.println("[**] [" + endIndex + ", " + output.get(endIndex) + "]");
			}
			assertTrue("The length of a class's (" + i + ") range (" + range + ") should never be negative. [" + classBounds[i - 1] + ", " + classBounds[i] + "]", range >= 0);
		}
	}

	private double[] getMinAndMax(ArrayList<NumericElement<Double>> input, int[] classBounds, int classification) {
		double[] minAndMax = new double[2];
		minAndMax[0] = Double.POSITIVE_INFINITY;
		minAndMax[1] = Double.NEGATIVE_INFINITY;

		final int startPosition = (classification == 0) ? 0 : (classBounds[classification - 1] + 1);
		final int endPosition = classBounds[classification];
		int currValue = 0;

		for (int i = startPosition; i <= endPosition; ++i) {
			currValue = input.get(i).getValue().intValue();
			if (currValue < minAndMax[0]) {
				minAndMax[0] = currValue;
			} else if (currValue > minAndMax[1]) {
				minAndMax[1] = currValue;
			}
		}

		return minAndMax;
	}
}
