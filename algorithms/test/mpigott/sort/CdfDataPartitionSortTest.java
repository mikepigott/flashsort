package mpigott.sort;

import static org.junit.Assert.*;

import mpigott.sort.CdfDataPartitionSort;

import org.junit.Test;

public class CdfDataPartitionSortTest {

	@Test
	public void testGetMinSamplesPerCategory() {
		assertEquals(12736, (int) CdfDataPartitionSort.getMinSamplesPerCategory(0.05, 0.01));
		assertEquals(40612, (int) CdfDataPartitionSort.getMinSamplesPerCategory(0.05, 0.0056));
	}

}
