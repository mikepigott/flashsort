package mpigott.sort;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Ignore;
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
		assertEquals(  0, (int) statistics.getStandardDeviation());
		assertEquals(100, (int) statistics.getMin());
		assertEquals(100, (int) statistics.getMax());
	}

	@Test
	public void testPartitionFunction4() {
		ArrayList<NumericElement<Double>> input = createNonRandomInput(10000, -50.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 100, 0.05, 0.01);

		SummaryStatistics statistics = getBucketStatistics(input, func);

		assertEquals(100, statistics.getN());
		assertEquals(  0, (int) statistics.getStandardDeviation());
		assertEquals(100, (int) statistics.getMin());
		assertEquals(100, (int) statistics.getMax());
	}

	@Test
	public void testNonRandomPartitioning1() {
		ArrayList<NumericElement<Double>> input = createNonRandomInput(10000, -50.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 100, 0.05, 0.01);

		int[] classBounds = CyclePartitioner.partition(input, func);

		checkInput(input, func, classBounds);
	}

	@Test
	public void testNonRandomPartitioning2() {
		ArrayList<NumericElement<Double>> input = createNonRandomInput(100000, -50.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 100, 0.05, 0.01);

		int[] classBounds = CyclePartitioner.partition(input, func);

		checkInput(input, func, classBounds);
	}

	@Test
	public void testRandomPartitioning1() {
		ArrayList<NumericElement<Double>> input = createRandomInput(10000, 50.0, 300000.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 100, 0.05, 0.01);

		int[] classBounds = CyclePartitioner.partition(input, func);

		checkInput(input, func, classBounds);
	}

	@Test
	public void testRandomPartitioning2() {
		ArrayList<NumericElement<Double>> input = createRandomInput(100000, -50.0, 250000.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 512, 0.05, 0.0056);

		int[] classBounds = CyclePartitioner.partition(input, func);

		checkInput(input, func, classBounds);
	}

	@Test
	public void testRandomPartitioning3() {
		ArrayList<NumericElement<Double>> input = createRandomInput(1000000, -250000.0, 1250000.0);

		CdfPartitionFunction<NumericElement<Double>, Double> func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(input, 4096, 0.05, 0.0056);

		int[] classBounds = CyclePartitioner.partition(input, func);

		checkInput(input, func, classBounds);
	}

	@Test
	public void testCdfRanges() {
		ArrayList<NumericElement<Double>> cdfPartitionInput1 = createStandardNormalRandomInput(1000000, 1250000.0);
		ArrayList<NumericElement<Double>> cdfPartitionInput2 = (ArrayList<NumericElement<Double>>) cdfPartitionInput1.clone();

		CdfPartitionFunction<NumericElement<Double>, Double> cdfPartition1Func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(cdfPartitionInput1, 10000, 0.05, 0.0056);

		int[] cdfPartition1ClassBounds = CyclePartitioner.partition(cdfPartitionInput1, cdfPartition1Func);

		SummaryStatistics cdfPartition1Statistics = getClassBoundsStatistics(cdfPartition1ClassBounds);

		CdfPartitionFunction<NumericElement<Double>, Double> cdfPartition2Func =
			new CdfPartitionFunction<NumericElement<Double>, Double>(cdfPartitionInput2, 1000, 0.05, 0.0056);

		int[] cdfPartition2ClassBounds = CyclePartitioner.partition(cdfPartitionInput2, cdfPartition2Func);

		SummaryStatistics cdfPartition2Statistics = getClassBoundsStatistics(cdfPartition2ClassBounds);

		assertTrue("The relative standard deviation of the output with larger class sizes should be smaller than the relative standard deviation of the output with smaller class sizes.", (cdfPartition1Statistics.getStandardDeviation() / cdfPartition1Statistics.getMean()) < (cdfPartition2Statistics.getStandardDeviation() / cdfPartition2Statistics.getMean()));
	}

	@Test
	public void testVariousSortersRandomInput() {
		ArrayList<NumericElement<Double>> cdfPartitionInput = createRandomInput(1000000, 0.0, 15000.0);
		ArrayList<NumericElement<Double>> flashSortInput = (ArrayList<NumericElement<Double>>) cdfPartitionInput.clone();

		CdfPartitionFunction<NumericElement<Double>, Double> cdfPartitionFunc =
			new CdfPartitionFunction<NumericElement<Double>, Double>(cdfPartitionInput, 10000, 0.05, 0.0056);

		int[] cdfPartitionClassBounds = CyclePartitioner.partition(cdfPartitionInput, cdfPartitionFunc);

		checkInput(cdfPartitionInput, cdfPartitionFunc, cdfPartitionClassBounds);

		FlashSortPartitionFunction<NumericElement<Double>, Double> fsPartitionFunc =
			new FlashSortPartitionFunction<NumericElement<Double>, Double>(flashSortInput, 100);

		int[] fsClassBounds = CyclePartitioner.partition(flashSortInput, fsPartitionFunc);

		checkInput(flashSortInput, fsPartitionFunc, fsClassBounds);

		SummaryStatistics cdfPartitionStatistics = getClassBoundsStatistics(cdfPartitionClassBounds);
		SummaryStatistics flashSortStatistics = getClassBoundsStatistics(fsClassBounds);

		assertTrue("For evenly-distributed random input, the standard deviation of flash sort should be smaller.", flashSortStatistics.getStandardDeviation() < cdfPartitionStatistics.getStandardDeviation());
	}

	@Test
	public void testVariousSortersStandardNormalRandomInput() {
		ArrayList<NumericElement<Double>> cdfPartitionInput = createStandardNormalRandomInput(1000000, 9000.0);
		ArrayList<NumericElement<Double>> flashSortInput = (ArrayList<NumericElement<Double>>) cdfPartitionInput.clone();

		CdfPartitionFunction<NumericElement<Double>, Double> cdfPartitionFunc =
			new CdfPartitionFunction<NumericElement<Double>, Double>(cdfPartitionInput, 10000, 0.05, 0.0056);

		int[] cdfPartitionClassBounds = CyclePartitioner.partition(cdfPartitionInput, cdfPartitionFunc);

		checkInput(cdfPartitionInput, cdfPartitionFunc, cdfPartitionClassBounds);

		FlashSortPartitionFunction<NumericElement<Double>, Double> fsPartitionFunc =
			new FlashSortPartitionFunction<NumericElement<Double>, Double>(flashSortInput, 100);

		int[] fsClassBounds = CyclePartitioner.partition(flashSortInput, fsPartitionFunc);

		checkInput(flashSortInput, fsPartitionFunc, fsClassBounds);

		SummaryStatistics cdfPartitionStatistics = getClassBoundsStatistics(cdfPartitionClassBounds);
		SummaryStatistics flashSortStatistics = getClassBoundsStatistics(fsClassBounds);

		assertTrue("For evenly-distributed random input, the standard deviation of CDF-based partitioning should be smaller.", flashSortStatistics.getStandardDeviation() > cdfPartitionStatistics.getStandardDeviation());
	}

	private ArrayList<NumericElement<Double>> createNonRandomInput(int numElements, double min) {
		ArrayList<NumericElement<Double>> input =
				new ArrayList<NumericElement<Double>>(numElements);

		for (int i = 0; i < numElements; ++i) {
			input.add(new NumericElement<Double>(min + i));
		}

		return input;
	}

	private ArrayList<NumericElement<Double>> createRandomInput(int numElems, double min, double max) {
		ArrayList<NumericElement<Double>> input = new ArrayList<NumericElement<Double>>(numElems);
		for (int i = 0; i < numElems; ++i) {
			input.add(new NumericElement<Double>(Double.NEGATIVE_INFINITY));
		}
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < numElems; ++i) {
			int index = random.nextInt(numElems);
			while (input.get(index % numElems).getValue() >= min) {
				++index;
			}
			input.set(index % numElems, new NumericElement<Double>(random.nextDouble() * (max - min) + min));
		}

		return input;
	}

	private ArrayList<NumericElement<Double>> createStandardNormalRandomInput(int numElems, double variance) {
		StandardNormalRandomNumberGenerator stdNormRand = new StandardNormalRandomNumberGenerator(variance);

		ArrayList<NumericElement<Double>> input = new ArrayList<NumericElement<Double>>(numElems);
		for (int i = 0; i < numElems; ++i) {
			input.add(new NumericElement<Double>(Double.NEGATIVE_INFINITY));
		}
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < numElems; ++i) {
			int index = random.nextInt(numElems);
			while (input.get(index % numElems).getValue() > Double.NEGATIVE_INFINITY) {
				++index;
			}
			input.set(index % numElems, new NumericElement<Double>(stdNormRand.getNextRandom()));
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

	private SummaryStatistics getClassBoundsStatistics(int[] classBounds) {
		SummaryStatistics classBoundsStatistics = new SummaryStatistics();
		int prevClassBound = 0;
		for (int classBound : classBounds) {
			classBoundsStatistics.addValue(classBound - prevClassBound);
			prevClassBound = classBound;
		}
		return classBoundsStatistics;
	}

	private void checkInput(ArrayList<NumericElement<Double>> output, PartitionFunction<NumericElement<Double>, Double> func, int[] classBounds) {
		assertNotNull(classBounds);

		// Confirm the max value of a class is less than the min value of the next upper class.
		double[] prevMinAndMax = getMinAndMax(output, classBounds, 0);
		double[] currMinAndMax = null;

		assertTrue("The element in the upper bound of the class 0 does not belong to class zero.", (func.getClass(output.get(classBounds[0])) == 0) || (classBounds[0] == 0));

		for (int i = 1; i < classBounds.length; ++i) {
			assertTrue("The element in the upper bound of the class " + i + " does not belong to class " + i + ".", (func.getClass(output.get(classBounds[i])) == i) || (classBounds[i] == classBounds[i - 1]));
			currMinAndMax = getMinAndMax(output, classBounds, i);
			final boolean isValid = prevMinAndMax[1] <= currMinAndMax[0];
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

	// From http://en.wikipedia.org/wiki/Box_Muller_transform
	private static class StandardNormalRandomNumberGenerator {
		private static final double TWO_PI = 6.2831853071795864769252866;

		public StandardNormalRandomNumberGenerator(double variance) {
			this.hasSpare = false;
			this.rand1 = 0.0;
			this.rand2 = 0.0;
			this.random = new Random(System.currentTimeMillis());
			this.variance = variance;
		}

		public double getNextRandom() {
			if (hasSpare) {
				hasSpare = false;
				return Math.sqrt(variance * rand1) + Math.sin(rand2);
			}

			hasSpare = true;

			rand1 = random.nextDouble();
			if (rand1 < 1e-100) {
				rand1 = 1e-100;
			}
			rand1 = -2 * Math.log(rand1);

			rand2 = random.nextDouble() * TWO_PI;

			return Math.sqrt(variance * rand1) * Math.cos(rand2);
		}

		private boolean hasSpare;
		private double rand1;
		private double rand2;
		private Random random;
		private double variance;
	}
}
