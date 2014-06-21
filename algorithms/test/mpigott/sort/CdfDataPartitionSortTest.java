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
		System.out.println(statistics);

		for (int i = 0; i < input.size(); ++i) {
			if (func.getClass(input.get(i)) == statistics.getMin()) {
				System.out.println("Bucket " + func.getClass(input.get(i)) + " contains the minumum of " + statistics.getMin() + " elements.");
				break;
			}
		}
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
}
