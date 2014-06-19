package mpigott.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import mpigott.sort.FlashSort;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

/**
 * Tests the flash sort implementation.
 *
 * @author Mike Pigott
 *
 */
public class SortTest {

	public static void main(String[] args) throws Exception {
		// If a (space-delimited) array is provided on the command-line, use it.
		if (args.length == 0) {
			SortTest test = new SortTest();
			test.randomFlashSortTest();
		} else {
			ArrayList<Integer> input = new ArrayList<Integer>(args.length - 1);
			for (int i = 1; i < args.length; ++i) {
				input.add(new Integer(args[i]));
			}

			FlashSort.sort(input, Integer.parseInt(args[0]));
		}
	}

	@Test
	public void nonRandomFlashSortTest() {
		ArrayList<Integer> input = createNonRandomInput(500);
		ArrayList<Integer> copy = (ArrayList<Integer>) input.clone();
		final int[] classBounds = FlashSort.sort(input, 50);
		checkInput(input, classBounds, copy, 50);
	}

	@Test //@Ignore
	public void randomFlashSortTest() {
		ArrayList<Integer> input = createRandomInput(1000, 500);
		ArrayList<Integer> copy = (ArrayList<Integer>) input.clone();
		int[] classBounds = null;
		try {
			classBounds = FlashSort.sort(input, 100);
		} catch (RuntimeException re) {
			printInputCode(input, 100);
		}
		checkInput(input, classBounds, copy, 100);
	}

	@Test
	public void randomFlashSortWithFewerValuesThanClassesTest() {
		ArrayList<Integer> input = createRandomInput(500, 50);
		ArrayList<Integer> copy = (ArrayList<Integer>) input.clone();
		int[] classBounds = null;
		try {
			classBounds = FlashSort.sort(input, 100);
		} catch (RuntimeException re) {
			printInputCode(input, 100);
		}
		checkInput(input, classBounds, copy, 100);
	}

	@Test
	public void flashSortTestWithClassIndexBinarySearchReturningPositiveAndNegativeValues() {
		int[] inputArray = { 342, 378, 414, 69, 329, 309, 341, 418, 87, 270, 323, 139, 314, 286, 374, 374, 211, 235, 234, 123, 318, 385, 186, 369, 321, 89, 301, 238, 247, 119, 79, 149, 166, 345, 287, 121, 155, 455, 36, 307, 61, 94, 210, 186, 215, 134, 438, 448, 107, 360, 352, 393, 181, 378, 274, 384, 449, 140, 420, 289, 303, 297, 318, 32, 321, 3, 344, 282, 407, 72, 406, 41, 274, 32, 340, 114, 105, 313, 441, 394, 442, 61, 469, 410, 8, 88, 228, 92, 278, 352, 35, 389, 451, 309, 355, 438, 323, 184, 146, 140, 10, 111, 263, 367, 35, 153, 135, 398, 434, 446, 46, 456, 359, 156, 253, 105, 490, 255, 179, 433, 399, 395, 459, 400, 344, 225, 178, 259, 1, 400, 286, 54, 242, 138, 380, 174, 308, 431, 189, 441, 439, 116, 448, 320, 41, 141, 29, 57, 489, 319, 83, 266, 21, 161, 361, 133, 311, 21, 445, 320, 435, 319, 225, 248, 151, 70, 151, 238, 348, 451, 122, 187, 197, 328, 308, 102, 137, 99, 239, 23, 455, 122, 465, 226, 487, 371, 294, 454, 32, 167, 16, 129, 379, 143, 82, 27, 251, 317, 206, 66, 317, 169, 322, 315, 44, 273, 134, 327, 257, 116, 485, 188, 329, 125, 167, 101, 30, 156, 111, 290, 369, 27, 115, 358, 281, 321, 443, 227, 307, 435, 263, 281, 426, 425, 20, 50, 426, 424, 284, 114, 400, 82, 358, 414, 158, 367, 141, 359, 256, 34, 275, 334, 406, 380, 22, 431, 68, 107, 196, 268, 343, 114, 299, 392, 397, 33, 130, 421, 322, 156, 367, 242, 389, 83, 172, 312, 218, 240, 266, 320, 478, 492, 73, 347, 419, 112, 362, 164, 378, 6, 495, 133, 43, 415, 270, 213, 7, 143, 38, 398, 483, 164, 255, 47, 225, 383, 224, 36, 490, 439, 50, 447, 55, 425, 235, 447, 2, 417, 124, 93, 72, 22, 72, 271, 365, 53, 155, 325, 495, 184, 344, 448, 382, 425, 335, 118, 152, 55, 145, 249, 192, 20, 201, 328, 233, 418, 320, 429, 14, 403, 253, 43, 46, 137, 61, 193, 106, 230, 467, 34, 490, 470, 402, 446, 73, 67, 17, 414, 367, 405, 220, 222, 23, 468, 241, 408, 5, 186, 247, 324, 436, 227, 278, 415, 472, 473, 214, 363, 436, 367, 499, 152, 65, 164, 411, 386, 344, 106, 34, 267, 74, 249, 455, 446, 230, 235, 255, 401, 133, 269, 219, 18, 386, 233, 230, 259, 441, 164, 453, 66, 3, 375, 315, 430, 50, 179, 81, 459, 132, 191, 55, 307, 161, 236, 52, 314, 71, 417, 175, 142, 453, 27, 126, 212, 202, 457, 303, 314, 105, 325, 173, 132, 374, 384, 464, 437, 126, 242, 36, 251, 410, 448, 131, 458, 187, 204, 17, 416, 424, 328, 142, 113, 496, 239, 5, 70, 466, 174, 465, 269, 356, 195, 94, 87, 209, 488, 290, 338, 85, 286, 22, 356, 201, 206, 264, 439, 24, 111, 206, 177};
		final int numClasses = 100;
		ArrayList<Integer> input = new ArrayList<Integer>(inputArray.length);
		for (int i = 0; i < inputArray.length; ++i) {
			input.add(inputArray[i]);
		}
		ArrayList<Integer> copy = (ArrayList<Integer>) input.clone();
		final int[] classBounds = FlashSort.sort(input, numClasses);
		checkInput(input, classBounds, copy, numClasses);
	}

	@Test
	public void flashSortTest5() {
		int[] inputArray = { 5, 5, 2, 1, 1, 3, 0, 1, 2, 4, 0, 0, 1, 3, 2, 5, 1, 2, 5, 3, 3, 5, 2, 0, 4, 0, 3, 5, 3, 5, 4, 3, 3, 2, 3, 2, 2, 5, 0, 4, 4, 2, 3, 3, 0, 5, 3, 4, 1, 4, 3, 3, 5, 3, 2, 5, 2, 2, 1, 7, 6, 6, 10, 9, 6, 6, 10, 9, 10, 9, 7, 6, 10, 7, 8, 6, 8, 6, 8, 7, 8, 6, 9, 10, 10, 7, 9, 7, 7, 6, 10, 8, 10, 7, 9, 9, 7, 8, 10, 9, 8, 10, 6, 11, 15, 15, 16, 15, 12, 11, 14, 13, 16, 11, 11, 14, 14, 14, 12, 14, 13, 15, 12, 16, 15, 16, 11, 12, 12, 11, 16, 12, 14, 12, 13, 16, 16, 16, 14, 11, 16, 15, 12, 16, 16, 11, 11, 11, 12, 15, 42, 17, 21, 18, 17, 19, 19, 21, 17, 19, 19, 18, 21, 21, 17, 17, 19, 17, 21, 21, 19, 17, 20, 17, 19, 20, 19, 17, 19, 17, 17, 21, 18, 21, 21, 17, 17, 18, 18, 20, 19, 20, 18, 20, 18, 17, 17, 18, 17, 18, 25, 26, 25, 25, 27, 26, 25, 26, 25, 26, 22, 26, 24, 25, 24, 27, 23, 26, 23, 27, 26, 25, 24, 25, 24, 24, 25, 25, 22, 22, 26, 22, 25, 25, 22, 27, 23, 22, 26, 24, 27, 27, 23, 27, 22, 26, 25, 25, 25, 27, 22, 23, 24, 27, 26, 24, 24, 24, 23, 27, 26, 26, 24, 25, 22, 22, 25, 31, 28, 29, 29, 30, 31, 28, 30, 31, 30, 31, 32, 32, 29, 29, 30, 29, 30, 31, 29, 31, 31, 31, 30, 32, 29, 31, 30, 29, 29, 32, 29, 30, 29, 31, 30, 32, 32, 32, 29, 30, 31, 32, 28, 32, 28, 32, 28, 28, 31, 30, 28, 30, 28, 30, 29, 30, 32, 30, 32, 37, 38, 36, 34, 36, 36, 36, 37, 37, 33, 34, 36, 38, 34, 37, 38, 35, 37, 33, 33, 33, 35, 37, 35, 38, 36, 35, 35, 38, 37, 33, 36, 36, 37, 33, 35, 34, 35, 37, 33, 34, 36, 37, 38, 37, 35, 36, 36, 38, 37, 38, 33, 33, 34, 36, 37, 38, 34, 37, 33, 36, 35, 41, 41, 43, 43, 42, 43, 40, 43, 41, 39, 40, 40, 41, 40, 42, 39, 43, 43, 39, 42, 40, 43, 40, 41, 43, 39, 42, 39, 43, 40, 39, 43, 40, 42, 40, 41, 40, 42, 40, 42, 39, 42, 40, 41, 40, 40, 43, 41, 39, 40, 40, 39, 43, 42, 39, 39, 46, 44, 44, 46, 46, 48, 47, 45, 48, 48, 44, 46, 45, 47, 44, 45, 48, 45, 45, 47, 45, 48, 45, 45, 48, 47, 45, 48, 45, 45, 47, 46, 48, 45, 44, 45, 46, 48, 46, 44, 47, 48, 48, 47, 45, 46, 49, 49, 49, 49, 49, 49, 49, 49, 49};
		final int numClasses = 100;
		ArrayList<Integer> input = new ArrayList<Integer>(inputArray.length);
		for (int i = 0; i < inputArray.length; ++i) {
			input.add(inputArray[i]);
		}
		ArrayList<Integer> copy = (ArrayList<Integer>) input.clone();
		final int[] classBounds = FlashSort.sort(input, numClasses);
		checkInput(input, classBounds, copy, numClasses);
	}

	public void flashSortTest500ElemsWithRange500() {
		int[] inputArray = { 257, 297, 431, 177, 64, 427, 239, 420, 70, 235, 280, 470, 72, 162, 448, 225, 334, 327, 266, 11, 204, 221, 322, 345, 168, 272, 343, 200, 197, 394, 132, 41, 306, 459, 241, 166, 351, 21, 194, 136, 76, 189, 417, 440, 360, 316, 75, 470, 228, 218, 33, 53, 346, 93, 434, 187, 192, 108, 53, 444, 459, 399, 205, 248, 454, 37, 74, 318, 193, 259, 407, 67, 176, 199, 276, 483, 114, 404, 63, 303, 212, 132, 119, 221, 256, 256, 191, 330, 293, 377, 42, 467, 66, 77, 461, 392, 371, 147, 294, 114, 495, 148, 444, 246, 240, 335, 327, 333, 328, 40, 235, 207, 270, 99, 141, 448, 315, 29, 331, 433, 183, 285, 403, 414, 495, 438, 439, 182, 347, 235, 29, 298, 187, 289, 75, 206, 88, 421, 313, 471, 193, 59, 480, 234, 394, 15, 4, 245, 66, 33, 459, 314, 46, 176, 405, 161, 56, 423, 416, 89, 101, 43, 345, 29, 27, 55, 404, 161, 312, 473, 260, 179, 374, 319, 169, 487, 207, 312, 233, 123, 426, 438, 438, 275, 84, 444, 11, 256, 99, 367, 30, 256, 152, 415, 468, 204, 172, 135, 10, 279, 54, 254, 465, 114, 20, 230, 177, 111, 325, 488, 218, 104, 300, 51, 123, 395, 374, 177, 412, 483, 132, 43, 482, 102, 253, 346, 16, 302, 484, 350, 368, 242, 146, 29, 18, 361, 489, 190, 216, 310, 343, 0, 0, 472, 479, 18, 403, 156, 346, 338, 60, 309, 253, 150, 6, 47, 181, 323, 473, 262, 485, 200, 336, 202, 179, 228, 329, 468, 171, 125, 181, 326, 194, 402, 335, 218, 27, 492, 428, 404, 322, 37, 327, 156, 173, 332, 56, 17, 369, 417, 462, 365, 105, 357, 215, 323, 461, 332, 337, 102, 54, 67, 319, 257, 167, 100, 307, 436, 114, 383, 343, 214, 242, 495, 78, 90, 121, 293, 71, 385, 121, 488, 44, 192, 232, 177, 138, 146, 278, 465, 61, 482, 192, 118, 83, 239, 417, 430, 489, 330, 239, 86, 395, 439, 368, 360, 318, 394, 212, 303, 286, 299, 392, 383, 151, 211, 499, 456, 31, 96, 104, 90, 48, 486, 15, 40, 193, 355, 55, 74, 269, 209, 116, 106, 196, 19, 445, 471, 201, 312, 497, 162, 493, 111, 415, 2, 268, 194, 310, 156, 129, 178, 121, 146, 443, 206, 240, 66, 60, 272, 380, 314, 202, 205, 463, 225, 332, 59, 144, 232, 339, 425, 212, 395, 322, 290, 491, 295, 493, 301, 131, 85, 403, 42, 380, 453, 72, 109, 492, 87, 314, 226, 252, 161, 298, 105, 216, 457, 46, 267, 75, 419, 129, 118, 104, 386, 398, 406, 427, 31, 154, 206, 277, 222, 294, 216, 247, 51, 275, 293, 430, 344, 429, 41, 213, 86, 493, 171, 143, 251, 219, 193, 88, 166, 403, 118, 293, 6, 172, 299, 36, 70, 345, 26, 3, 246, 396, 221, 43, 122, 349, 281, 68, 180, 495, 174, 279, 462, 138, 122};
		final int numClasses = 100;
		ArrayList<Integer> input = new ArrayList<Integer>(inputArray.length);
		for (int i = 0; i < inputArray.length; ++i) {
			input.add(inputArray[i]);
		}
		ArrayList<Integer> copy = (ArrayList<Integer>) input.clone();
		final int[] classBounds = FlashSort.sort(input, numClasses);
		checkInput(input, classBounds, copy, numClasses);
	}

	private void checkInput(ArrayList<Integer> input, int[] classBounds, ArrayList<Integer> origArray, int numOrigClasses) {
		if (classBounds == null) {
			printInputCode(origArray, numOrigClasses);
		}
		assertNotNull(classBounds);

		// Confirm the max value of a class is less than the min value of the next upper class.
		int[] prevMinAndMax = getMinAndMax(input, classBounds, 0);
		int[] currMinAndMax = null;

		for (int i = 1; i < classBounds.length; ++i) {
			currMinAndMax = getMinAndMax(input, classBounds, i);
			final boolean isValid = prevMinAndMax[1] < currMinAndMax[0];
			if (!isValid) {
				printInputCode(origArray, numOrigClasses);
			}
			assertTrue("Maximum of class " + (i - 1) + " (" + prevMinAndMax[1] + ") must be less than the min of class " + i + " (" + currMinAndMax[0] + ").", isValid);
			prevMinAndMax = currMinAndMax;
		}

		// Confirm the ranges are valid.
		for (int i = 1; i < classBounds.length; ++i) {
			final int range = (classBounds[i] - classBounds[i - 1]);
			if (range < 0) {
				printInputCode(origArray, numOrigClasses);
			}
			assertTrue("The length of a range (" + range + ") should never be negative. [" + classBounds[i - 1] + ", " + classBounds[i] + "]", range >= 0);
		}
	}

	private int[] getMinAndMax(ArrayList<Integer> input, int[] classBounds, int classification) {
		int[] minAndMax = new int[2];
		minAndMax[0] = Integer.MAX_VALUE;
		minAndMax[1] = Integer.MIN_VALUE;

		final int startPosition = (classification == 0) ? 0 : (classBounds[classification - 1] + 1);
		final int endPosition = classBounds[classification];
		int currValue = 0;

		for (int i = startPosition; i <= endPosition; ++i) {
			currValue = input.get(i).intValue();
			if (currValue < minAndMax[0]) {
				minAndMax[0] = currValue;
			} else if (currValue > minAndMax[1]) {
				minAndMax[1] = currValue;
			}
		}

		return minAndMax;
	}

	private ArrayList<Integer> createNonRandomInput(int numElems) {
		ArrayList<Integer> input = new ArrayList<Integer>(numElems);
		for (int i = 0; i < numElems; ++i) {
			input.add(0);
		}
		Random random = new Random();
		for (int i = 0; i < numElems; ++i) {
			int index = random.nextInt(numElems);
			while (input.get(index % numElems) > 0) {
				++index;
			}
			input.set(index % numElems, i + 1);
		}

		return input;
	}

	private ArrayList<Integer> createRandomInput(int numElems, int maxValue) {
		ArrayList<Integer> input = new ArrayList<Integer>(numElems);
		for (int i = 0; i < numElems; ++i) {
			input.add(0);
		}
		Random random = new Random();
		for (int i = 0; i < numElems; ++i) {
			int index = random.nextInt(numElems);
			while (input.get(index % numElems) > 0) {
				++index;
			}
			input.set(index % numElems, random.nextInt(maxValue));
		}

		return input;
	}

	private void printInputCode(ArrayList<Integer> input, int numClasses) {
		System.out.print("int[] inputArray = { ");
		for (int i = 0; i < (input.size() - 1); ++i) {
			System.out.print(input.get(i) + ", ");
		}
		System.out.println(input.get(input.size() - 1) + " };");
		System.out.println("final int numClasses = " + numClasses + ";");
		System.out.println("ArrayList<Integer> input = new ArrayList<Integer>(inputArray.length);");
		System.out.println("for (int i = 0; i < inputArray.length; ++i) {");
		System.out.println("\tinput.add(inputArray[i]);");
		System.out.println("}");
		System.out.println("final int[] classBounds = FlashSort.sort(input, numClasses, true);");
		System.out.println("checkInput(input, classBounds);");
	}

	private void printClassSizes(int[] classBounds) {
		int min = (classBounds[classBounds.length - 1] - classBounds[classBounds.length - 2]);
		int minStart = classBounds.length - 2;
		int minEnd = classBounds.length - 1;

		int max = min;
		int maxStart = minStart;
		int maxEnd = maxStart;

		int totalRangeSizes = min;
		SummaryStatistics statistics = new SummaryStatistics();
		statistics.addValue(min);

		for (int i = 0; i < (classBounds.length - 1); ++i) {
			if (i > 1) {
				final int range = (classBounds[i] - classBounds[i - 1]);
				statistics.addValue(range);
				totalRangeSizes += range;
				if (range < min) {
					min = range;
					minStart = classBounds[i - 1];
					minEnd = classBounds[i];
				} else if (range > max) {
					max = range;
					maxStart = classBounds[i - 1];
					maxEnd = classBounds[i];
				}
			}
			System.out.print("[" + i + ": " + classBounds[i] + "], ");
		}
		System.out.println("[" + (classBounds.length - 1) + ", " + classBounds[classBounds.length - 1] + "]");
		System.out.println("Min Range: " + min + " [" + minStart + ", " + minEnd + "]; Max Range: " + max + " [" + maxStart + ", " + maxEnd + "]");
		System.out.println("Average Range Size: " + (totalRangeSizes / classBounds.length + 1));
		System.out.println(statistics);
	}
}
