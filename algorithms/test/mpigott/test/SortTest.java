package mpigott.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import mpigott.sort.CyclePartitioner;
import mpigott.sort.FlashSortPartitionFunction;
import mpigott.sort.NumericElement;

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
			ArrayList<NumericElement<Integer>> input = new ArrayList<NumericElement<Integer>>(args.length - 1);
			for (int i = 1; i < args.length; ++i) {
				input.add(new NumericElement<Integer>( new Integer(args[i]) ));
			}

			CyclePartitioner.partition(input, new FlashSortPartitionFunction<NumericElement<Integer>, Integer>(input, Integer.parseInt(args[0])));
		}
	}

	@Test
	public void nonRandomFlashSortTest() {
		ArrayList<NumericElement<Integer>> input = createNonRandomInput(500);
		ArrayList<NumericElement<Integer>> copy = (ArrayList<NumericElement<Integer>>) input.clone();
		final int[] classBounds = CyclePartitioner.partition(input, new FlashSortPartitionFunction<NumericElement<Integer>, Integer>(input, 50));
		checkInput(input, classBounds, copy, 50);
	}

	@Test
	public void randomFlashSortTest() {
		ArrayList<NumericElement<Integer>> input = createRandomInput(1000, 2000);
		ArrayList<NumericElement<Integer>> copy = (ArrayList<NumericElement<Integer>>) input.clone();
		int numClasses = 40; // Recommendation is n*0.42
		int[] classBounds = null;
		try {
			classBounds = CyclePartitioner.partition(input, new FlashSortPartitionFunction<NumericElement<Integer>, Integer>(input, numClasses));
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		checkInput(input, classBounds, copy, numClasses);
	}

	@Test
	public void randomFlashSortWithFewerValuesThanClassesTest() {
		ArrayList<NumericElement<Integer>> input = createRandomInput(500, 50);
		ArrayList<NumericElement<Integer>> copy = (ArrayList<NumericElement<Integer>>) input.clone();
		int[] classBounds = null;
		try {
			classBounds = CyclePartitioner.partition(input, new FlashSortPartitionFunction<NumericElement<Integer>, Integer>(input, 100));
		} catch (RuntimeException re) {
			printInputCode(input, 100);
		}
		checkInput(input, classBounds, copy, 100);
	}

	@Test
	public void flashSortTestWithClassIndexBinarySearchReturningPositiveAndNegativeValues() {
		int[] inputArray = { 342, 378, 414, 69, 329, 309, 341, 418, 87, 270, 323, 139, 314, 286, 374, 374, 211, 235, 234, 123, 318, 385, 186, 369, 321, 89, 301, 238, 247, 119, 79, 149, 166, 345, 287, 121, 155, 455, 36, 307, 61, 94, 210, 186, 215, 134, 438, 448, 107, 360, 352, 393, 181, 378, 274, 384, 449, 140, 420, 289, 303, 297, 318, 32, 321, 3, 344, 282, 407, 72, 406, 41, 274, 32, 340, 114, 105, 313, 441, 394, 442, 61, 469, 410, 8, 88, 228, 92, 278, 352, 35, 389, 451, 309, 355, 438, 323, 184, 146, 140, 10, 111, 263, 367, 35, 153, 135, 398, 434, 446, 46, 456, 359, 156, 253, 105, 490, 255, 179, 433, 399, 395, 459, 400, 344, 225, 178, 259, 1, 400, 286, 54, 242, 138, 380, 174, 308, 431, 189, 441, 439, 116, 448, 320, 41, 141, 29, 57, 489, 319, 83, 266, 21, 161, 361, 133, 311, 21, 445, 320, 435, 319, 225, 248, 151, 70, 151, 238, 348, 451, 122, 187, 197, 328, 308, 102, 137, 99, 239, 23, 455, 122, 465, 226, 487, 371, 294, 454, 32, 167, 16, 129, 379, 143, 82, 27, 251, 317, 206, 66, 317, 169, 322, 315, 44, 273, 134, 327, 257, 116, 485, 188, 329, 125, 167, 101, 30, 156, 111, 290, 369, 27, 115, 358, 281, 321, 443, 227, 307, 435, 263, 281, 426, 425, 20, 50, 426, 424, 284, 114, 400, 82, 358, 414, 158, 367, 141, 359, 256, 34, 275, 334, 406, 380, 22, 431, 68, 107, 196, 268, 343, 114, 299, 392, 397, 33, 130, 421, 322, 156, 367, 242, 389, 83, 172, 312, 218, 240, 266, 320, 478, 492, 73, 347, 419, 112, 362, 164, 378, 6, 495, 133, 43, 415, 270, 213, 7, 143, 38, 398, 483, 164, 255, 47, 225, 383, 224, 36, 490, 439, 50, 447, 55, 425, 235, 447, 2, 417, 124, 93, 72, 22, 72, 271, 365, 53, 155, 325, 495, 184, 344, 448, 382, 425, 335, 118, 152, 55, 145, 249, 192, 20, 201, 328, 233, 418, 320, 429, 14, 403, 253, 43, 46, 137, 61, 193, 106, 230, 467, 34, 490, 470, 402, 446, 73, 67, 17, 414, 367, 405, 220, 222, 23, 468, 241, 408, 5, 186, 247, 324, 436, 227, 278, 415, 472, 473, 214, 363, 436, 367, 499, 152, 65, 164, 411, 386, 344, 106, 34, 267, 74, 249, 455, 446, 230, 235, 255, 401, 133, 269, 219, 18, 386, 233, 230, 259, 441, 164, 453, 66, 3, 375, 315, 430, 50, 179, 81, 459, 132, 191, 55, 307, 161, 236, 52, 314, 71, 417, 175, 142, 453, 27, 126, 212, 202, 457, 303, 314, 105, 325, 173, 132, 374, 384, 464, 437, 126, 242, 36, 251, 410, 448, 131, 458, 187, 204, 17, 416, 424, 328, 142, 113, 496, 239, 5, 70, 466, 174, 465, 269, 356, 195, 94, 87, 209, 488, 290, 338, 85, 286, 22, 356, 201, 206, 264, 439, 24, 111, 206, 177};
		final int numClasses = 100;
		ArrayList<NumericElement<Integer>> input = new ArrayList<NumericElement<Integer>>(inputArray.length);
		for (int i = 0; i < inputArray.length; ++i) {
			input.add( new NumericElement<Integer>(inputArray[i]) );
		}
		ArrayList<NumericElement<Integer>> copy = (ArrayList<NumericElement<Integer>>) input.clone();
		final int[] classBounds = CyclePartitioner.partition(input, new FlashSortPartitionFunction<NumericElement<Integer>, Integer>(input, numClasses));
		checkInput(input, classBounds, copy, numClasses);
	}

	@Test
	public void flashSortCascadingDownwardsTest() {
		int[] inputArray = { 5, 5, 2, 1, 1, 3, 0, 1, 2, 4, 0, 0, 1, 3, 2, 5, 1, 2, 5, 3, 3, 5, 2, 0, 4, 0, 3, 5, 3, 5, 4, 3, 3, 2, 3, 2, 2, 5, 0, 4, 4, 2, 3, 3, 0, 5, 3, 4, 1, 4, 3, 3, 5, 3, 2, 5, 2, 2, 1, 7, 6, 6, 10, 9, 6, 6, 10, 9, 10, 9, 7, 6, 10, 7, 8, 6, 8, 6, 8, 7, 8, 6, 9, 10, 10, 7, 9, 7, 7, 6, 10, 8, 10, 7, 9, 9, 7, 8, 10, 9, 8, 10, 6, 11, 15, 15, 16, 15, 12, 11, 14, 13, 16, 11, 11, 14, 14, 14, 12, 14, 13, 15, 12, 16, 15, 16, 11, 12, 12, 11, 16, 12, 14, 12, 13, 16, 16, 16, 14, 11, 16, 15, 12, 16, 16, 11, 11, 11, 12, 15, 42, 17, 21, 18, 17, 19, 19, 21, 17, 19, 19, 18, 21, 21, 17, 17, 19, 17, 21, 21, 19, 17, 20, 17, 19, 20, 19, 17, 19, 17, 17, 21, 18, 21, 21, 17, 17, 18, 18, 20, 19, 20, 18, 20, 18, 17, 17, 18, 17, 18, 25, 26, 25, 25, 27, 26, 25, 26, 25, 26, 22, 26, 24, 25, 24, 27, 23, 26, 23, 27, 26, 25, 24, 25, 24, 24, 25, 25, 22, 22, 26, 22, 25, 25, 22, 27, 23, 22, 26, 24, 27, 27, 23, 27, 22, 26, 25, 25, 25, 27, 22, 23, 24, 27, 26, 24, 24, 24, 23, 27, 26, 26, 24, 25, 22, 22, 25, 31, 28, 29, 29, 30, 31, 28, 30, 31, 30, 31, 32, 32, 29, 29, 30, 29, 30, 31, 29, 31, 31, 31, 30, 32, 29, 31, 30, 29, 29, 32, 29, 30, 29, 31, 30, 32, 32, 32, 29, 30, 31, 32, 28, 32, 28, 32, 28, 28, 31, 30, 28, 30, 28, 30, 29, 30, 32, 30, 32, 37, 38, 36, 34, 36, 36, 36, 37, 37, 33, 34, 36, 38, 34, 37, 38, 35, 37, 33, 33, 33, 35, 37, 35, 38, 36, 35, 35, 38, 37, 33, 36, 36, 37, 33, 35, 34, 35, 37, 33, 34, 36, 37, 38, 37, 35, 36, 36, 38, 37, 38, 33, 33, 34, 36, 37, 38, 34, 37, 33, 36, 35, 41, 41, 43, 43, 42, 43, 40, 43, 41, 39, 40, 40, 41, 40, 42, 39, 43, 43, 39, 42, 40, 43, 40, 41, 43, 39, 42, 39, 43, 40, 39, 43, 40, 42, 40, 41, 40, 42, 40, 42, 39, 42, 40, 41, 40, 40, 43, 41, 39, 40, 40, 39, 43, 42, 39, 39, 46, 44, 44, 46, 46, 48, 47, 45, 48, 48, 44, 46, 45, 47, 44, 45, 48, 45, 45, 47, 45, 48, 45, 45, 48, 47, 45, 48, 45, 45, 47, 46, 48, 45, 44, 45, 46, 48, 46, 44, 47, 48, 48, 47, 45, 46, 49, 49, 49, 49, 49, 49, 49, 49, 49};
		final int numClasses = 100;
		ArrayList<NumericElement<Integer>> input = new ArrayList<NumericElement<Integer>>(inputArray.length);
		for (int i = 0; i < inputArray.length; ++i) {
			input.add(new NumericElement<Integer>(inputArray[i]));
		}
		ArrayList<NumericElement<Integer>> copy = (ArrayList<NumericElement<Integer>>) input.clone();
		final int[] classBounds = CyclePartitioner.partition(input, new FlashSortPartitionFunction<NumericElement<Integer>, Integer>(input, numClasses));
		checkInput(input, classBounds, copy, numClasses);
	}

	@Test
	public void flashSortCascadingUpwardsTest() {
		int[] inputArray = { 352, 392, 190, 221, 58, 35, 345, 173, 38, 150, 207, 27, 176, 171, 112, 284, 440, 107, 409, 78, 345, 205, 386, 173, 422, 384, 122, 363, 434, 163, 275, 409, 473, 179, 131, 378, 493, 156, 224, 344, 262, 288, 344, 361, 308, 383, 426, 159, 139, 410, 59, 249, 400, 203, 156, 442, 467, 414, 406, 345, 91, 348, 427, 294, 432, 116, 176, 333, 180, 158, 495, 193, 134, 117, 22, 340, 441, 37, 51, 32, 0, 358, 86, 122, 0, 454, 0, 144, 16, 330, 48, 303, 123, 74, 246, 158, 492, 17, 391, 234, 431, 17, 492, 493, 154, 361, 496, 390, 409, 147, 115, 300, 111, 25, 129, 381, 5, 31, 46, 151, 102, 17, 227, 237, 236, 246, 85, 366, 41, 379, 36, 170, 69, 334, 376, 103, 442, 329, 359, 91, 62, 125, 375, 50, 129, 75, 80, 139, 286, 285, 70, 447, 191, 412, 340, 331, 118, 313, 100, 57, 81, 30, 51, 175, 224, 77, 179, 355, 239, 17, 94, 319, 258, 391, 414, 177, 321, 406, 339, 238, 178, 156, 151, 498, 472, 463, 406, 78, 169, 13, 131, 365, 446, 76, 185, 170, 451, 46, 491, 312, 272, 158, 426, 240, 197, 182, 243, 495, 327, 469, 295, 191, 384, 48, 30, 134, 380, 137, 386, 193, 130, 315, 442, 349, 211, 335, 207, 171, 167, 384, 57, 199, 301, 464, 237, 226, 343, 447, 375, 2, 452, 327, 74, 497, 355, 135, 346, 97, 381, 367, 289, 380, 179, 22, 131, 479, 164, 388, 329, 183, 208, 167, 16, 431, 477, 41, 466, 229, 122, 94, 247, 231, 421, 188, 5, 191, 284, 317, 244, 309, 354, 26, 77, 27, 56, 56, 89, 13, 415, 415, 492, 39, 61, 137, 354, 185, 264, 260, 214, 371, 355, 326, 358, 176, 235, 271, 441, 295, 483, 321, 461, 184, 476, 62, 6, 424, 86, 94, 240, 78, 111, 53, 370, 180, 200, 220, 379, 71, 368, 390, 487, 249, 53, 153, 99, 110, 96, 365, 149, 234, 76, 419, 380, 198, 219, 206, 373, 188, 207, 116, 250, 479, 432, 495, 360, 99, 481, 27, 128, 374, 345, 28, 306, 174, 205, 227, 38, 466, 277, 408, 230, 111, 312, 342, 223, 328, 330, 101, 187, 447, 468, 268, 417, 116, 261, 247, 47, 249, 51, 277, 434, 283, 327, 9, 60, 180, 419, 72, 125, 444, 159, 117, 441, 345, 39, 98, 199, 335, 42, 295, 189, 395, 412, 211, 149, 78, 495, 233, 281, 247, 342, 311, 200, 499, 262, 457, 124, 364, 333, 131, 308, 105, 168, 194, 295, 55, 457, 468, 426, 199, 208, 443, 417, 270, 127, 343, 8, 499, 399, 79, 357, 327, 30, 286, 69, 157, 314, 380, 30, 300, 125, 31, 77, 9, 408, 378, 331, 407, 341, 413, 22, 228, 275, 334, 213, 55, 302, 468, 238, 325, 137, 90, 449, 392, 66, 16, 235, 270, 249, 457, 96, 10, 32, 462, 54, 2, 266, 389, 483, 87, 155, 415, 92, 131, 204, 406, 446, 493, 212, 261, 486, 436, 489, 163, 370, 239, 334, 309, 174, 69, 249, 207, 301, 169, 323, 69, 339, 224, 150, 317, 235, 322, 171, 102, 319, 398, 297, 36, 265, 389, 96, 191, 107, 181, 389, 42, 172, 38, 128, 5, 420, 246, 225, 211, 334, 477, 364, 302, 56, 224, 224, 199, 365, 498, 293, 132, 440, 486, 488, 99, 376, 164, 45, 41, 307, 368, 152, 330, 266, 353, 287, 431, 93, 272, 13, 35, 166, 254, 450, 104, 36, 421, 40, 258, 4, 371, 84, 348, 3, 289, 388, 394, 25, 276, 41, 277, 51, 265, 345, 320, 315, 428, 351, 482, 358, 200, 211, 188, 137, 167, 43, 145, 118, 251, 368, 48, 490, 16, 270, 405, 379, 463, 42, 163, 244, 439, 88, 297, 92, 2, 217, 279, 61, 438, 307, 236, 165, 203, 471, 454, 79, 114, 135, 146, 497, 113, 44, 245, 53, 279, 181, 462, 320, 233, 443, 295, 255, 386, 486, 38, 366, 99, 476, 46, 50, 321, 399, 254, 411, 32, 288, 125, 32, 341, 180, 16, 382, 252, 478, 280, 373, 204, 254, 194, 193, 38, 454, 191, 173, 494, 305, 344, 397, 448, 401, 49, 345, 498, 52, 245, 378, 343, 202, 290, 496, 183, 469, 395, 305, 422, 374, 461, 58, 303, 346, 457, 309, 410, 59, 10, 398, 321, 212, 439, 398, 138, 313, 391, 223, 276, 404, 410, 416, 488, 135, 472, 47, 126, 409, 4, 110, 346, 441, 106, 16, 6, 414, 59, 216, 468, 453, 315, 59, 209, 460, 268, 303, 111, 301, 275, 196, 468, 79, 490, 385, 130, 167, 25, 374, 248, 309, 248, 442, 311, 182, 130, 157, 147, 261, 76, 487, 260, 355, 475, 68, 92, 12, 190, 388, 201, 108, 1, 49, 261, 124, 456, 413, 115, 436, 79, 15, 162, 131, 156, 383, 286, 233, 38, 296, 486, 473, 112, 152, 290, 126, 60, 304, 420, 452, 192, 225, 275, 276, 300, 326, 420, 117, 189, 185, 294, 369, 150, 343, 393, 104, 131, 280, 190, 363, 441, 318, 185, 340, 369, 78, 210, 149, 66, 425, 87, 297, 288, 102, 67, 54, 47, 174, 435, 115, 71, 134, 271, 435, 60, 441, 118, 391, 197, 218, 3, 158, 278, 85, 212, 207, 228, 314, 200, 200, 226, 150, 64, 417, 82, 451, 340, 358, 74, 198, 31, 324, 162, 424, 83, 388, 397, 325, 448, 430, 398, 1, 312, 255, 441, 465, 495, 338, 76, 481, 309, 213, 468, 101, 10, 55, 73, 486, 395, 485, 46, 109, 106, 493, 428, 271, 96, 12, 204, 391, 490, 129, 7, 313, 320, 30, 397, 391, 380, 347, 81, 496, 307, 335, 12, 173, 237, 317, 108, 413, 148, 99, 271, 223, 188, 409, 248, 358, 339, 157, 4, 403, 250, 475, 297, 88, 212, 94, 342, 287, 446, 119, 167, 459, 360, 228, 124, 190, 382, 189, 489, 142, 270, 485, 321, 284, 138, 391, 444, 435, 299 };
		final int numClasses = 420;
		ArrayList<NumericElement<Integer>> input = new ArrayList<NumericElement<Integer>>(inputArray.length);
		for (int i = 0; i < inputArray.length; ++i) {
			input.add( new NumericElement<Integer>(inputArray[i]) );
		}
		ArrayList<NumericElement<Integer>> copy = (ArrayList<NumericElement<Integer>>) input.clone();
		final int[] classBounds = CyclePartitioner.partition(input, new FlashSortPartitionFunction<NumericElement<Integer>, Integer>(input, numClasses));
		checkInput(input, classBounds, copy, numClasses);
	}

	@Test
	public void flashSortTest500ElemsWithRange500() {
		int[] inputArray = { 257, 297, 431, 177, 64, 427, 239, 420, 70, 235, 280, 470, 72, 162, 448, 225, 334, 327, 266, 11, 204, 221, 322, 345, 168, 272, 343, 200, 197, 394, 132, 41, 306, 459, 241, 166, 351, 21, 194, 136, 76, 189, 417, 440, 360, 316, 75, 470, 228, 218, 33, 53, 346, 93, 434, 187, 192, 108, 53, 444, 459, 399, 205, 248, 454, 37, 74, 318, 193, 259, 407, 67, 176, 199, 276, 483, 114, 404, 63, 303, 212, 132, 119, 221, 256, 256, 191, 330, 293, 377, 42, 467, 66, 77, 461, 392, 371, 147, 294, 114, 495, 148, 444, 246, 240, 335, 327, 333, 328, 40, 235, 207, 270, 99, 141, 448, 315, 29, 331, 433, 183, 285, 403, 414, 495, 438, 439, 182, 347, 235, 29, 298, 187, 289, 75, 206, 88, 421, 313, 471, 193, 59, 480, 234, 394, 15, 4, 245, 66, 33, 459, 314, 46, 176, 405, 161, 56, 423, 416, 89, 101, 43, 345, 29, 27, 55, 404, 161, 312, 473, 260, 179, 374, 319, 169, 487, 207, 312, 233, 123, 426, 438, 438, 275, 84, 444, 11, 256, 99, 367, 30, 256, 152, 415, 468, 204, 172, 135, 10, 279, 54, 254, 465, 114, 20, 230, 177, 111, 325, 488, 218, 104, 300, 51, 123, 395, 374, 177, 412, 483, 132, 43, 482, 102, 253, 346, 16, 302, 484, 350, 368, 242, 146, 29, 18, 361, 489, 190, 216, 310, 343, 0, 0, 472, 479, 18, 403, 156, 346, 338, 60, 309, 253, 150, 6, 47, 181, 323, 473, 262, 485, 200, 336, 202, 179, 228, 329, 468, 171, 125, 181, 326, 194, 402, 335, 218, 27, 492, 428, 404, 322, 37, 327, 156, 173, 332, 56, 17, 369, 417, 462, 365, 105, 357, 215, 323, 461, 332, 337, 102, 54, 67, 319, 257, 167, 100, 307, 436, 114, 383, 343, 214, 242, 495, 78, 90, 121, 293, 71, 385, 121, 488, 44, 192, 232, 177, 138, 146, 278, 465, 61, 482, 192, 118, 83, 239, 417, 430, 489, 330, 239, 86, 395, 439, 368, 360, 318, 394, 212, 303, 286, 299, 392, 383, 151, 211, 499, 456, 31, 96, 104, 90, 48, 486, 15, 40, 193, 355, 55, 74, 269, 209, 116, 106, 196, 19, 445, 471, 201, 312, 497, 162, 493, 111, 415, 2, 268, 194, 310, 156, 129, 178, 121, 146, 443, 206, 240, 66, 60, 272, 380, 314, 202, 205, 463, 225, 332, 59, 144, 232, 339, 425, 212, 395, 322, 290, 491, 295, 493, 301, 131, 85, 403, 42, 380, 453, 72, 109, 492, 87, 314, 226, 252, 161, 298, 105, 216, 457, 46, 267, 75, 419, 129, 118, 104, 386, 398, 406, 427, 31, 154, 206, 277, 222, 294, 216, 247, 51, 275, 293, 430, 344, 429, 41, 213, 86, 493, 171, 143, 251, 219, 193, 88, 166, 403, 118, 293, 6, 172, 299, 36, 70, 345, 26, 3, 246, 396, 221, 43, 122, 349, 281, 68, 180, 495, 174, 279, 462, 138, 122};
		final int numClasses = 100;
		ArrayList<NumericElement<Integer>> input = new ArrayList<NumericElement<Integer>>(inputArray.length);
		for (int i = 0; i < inputArray.length; ++i) {
			input.add(new NumericElement<Integer>(inputArray[i]));
		}
		ArrayList<NumericElement<Integer>> copy = (ArrayList<NumericElement<Integer>>) input.clone();
		final int[] classBounds = CyclePartitioner.partition(input, new FlashSortPartitionFunction<NumericElement<Integer>, Integer>(input, numClasses));
		checkInput(input, classBounds, copy, numClasses);
	}

	@Test
	public void flashSortTest7() {
		int[] inputArray = { 48, 29, 144, 456, 363, 198, 206, 143, 471, 112, 281, 103, 51, 319, 400, 305, 183, 191, 1, 158, 371, 489, 472, 222, 457, 152, 328, 91, 78, 46, 74, 445, 202, 453, 12, 170, 51, 410, 102, 8, 141, 255, 85, 43, 80, 52, 466, 497, 422, 460, 188, 273, 240, 4, 236, 344, 475, 14, 23, 329, 434, 198, 245, 314, 198, 354, 339, 69, 175, 365, 288, 23, 114, 252, 298, 209, 308, 251, 333, 191, 131, 160, 237, 470, 298, 431, 52, 211, 371, 312, 282, 480, 482, 52, 296, 168, 481, 71, 112, 326, 482, 424, 364, 380, 291, 76, 306, 450, 126, 116, 414, 95, 271, 463, 113, 114, 96, 373, 407, 431, 429, 16, 174, 176, 150, 94, 308, 470, 388, 323, 477, 471, 459, 270, 443, 437, 106, 131, 427, 97, 20, 477, 478, 498, 344, 485, 349, 14, 456, 159, 414, 333, 419, 80, 496, 323, 121, 146, 251, 391, 413, 118, 224, 79, 227, 444, 329, 272, 233, 48, 299, 452, 306, 454, 400, 178, 369, 136, 403, 96, 93, 244, 343, 420, 223, 394, 285, 67, 4, 188, 275, 159, 441, 271, 329, 476, 294, 494, 261, 38, 491, 172, 121, 204, 79, 89, 390, 356, 282, 452, 122, 83, 194, 258, 452, 347, 287, 84, 117, 455, 307, 4, 255, 49, 438, 280, 281, 330, 55, 190, 468, 208, 174, 203, 111, 455, 460, 495, 30, 4, 341, 104, 190, 74, 106, 434, 189, 340, 380, 276, 14, 107, 397, 227, 476, 84, 432, 129, 60, 205, 184, 212, 464, 152, 134, 164, 339, 66, 175, 216, 63, 21, 407, 284, 405, 385, 289, 490, 407, 135, 232, 65, 364, 179, 131, 491, 481, 145, 499, 448, 193, 152, 191, 202, 56, 363, 69, 387, 119, 86, 89, 209, 231, 279, 0, 0, 182, 493, 303, 96, 272, 207, 325, 10, 47, 131, 387, 213, 117, 318, 259, 178, 472, 368, 220, 35, 37, 334, 330, 48, 447, 392, 233, 487, 49, 246, 364, 211, 490, 306, 335, 161, 336, 79, 188, 328, 89, 336, 438, 474, 485, 266, 208, 225, 326, 110, 190, 137, 338, 182, 304, 144, 370, 162, 284, 122, 383, 5, 413, 115, 22, 192, 97, 132, 236, 190, 411, 454, 239, 74, 1, 135, 293, 3, 145, 497, 117, 409, 336, 398, 212, 98, 167, 367, 359, 169, 195, 149, 339, 72, 267, 232, 377, 334, 188, 9, 426, 471, 167, 92, 321, 313, 372, 51, 421, 416, 447, 409, 270, 343, 32, 0, 15, 338, 87, 6, 342, 87, 177, 224, 461, 17, 108, 13, 443, 356, 373, 268, 419, 242, 70, 271, 241, 55, 414, 36, 307, 29, 20, 210, 395, 163, 491, 402, 444, 182, 425, 396, 283, 176, 10, 281, 348, 93, 472, 7, 39, 261, 405, 205, 32, 42, 217, 306, 218, 361, 143, 443, 370, 185, 59, 315, 488, 376, 273, 219, 150, 267, 286, 284, 69, 348, 28, 288, 210, 434, 476, 102, 298, 20, 145, 358, 218, 322, 130, 161, 493, 286, 425, 344, 328, 474, 294, 155, 194, 223, 164, 404, 99, 404, 486, 480, 403, 4, 83, 299, 263, 7, 117, 100, 453, 190, 76, 132, 261, 321, 231, 207, 486, 313, 218, 70, 142, 479, 458, 271, 335, 66, 207, 109, 267, 109, 265, 157, 203, 125, 356, 375, 227, 388, 20, 162, 457, 245, 204, 273, 52, 249, 131, 487, 49, 497, 292, 492, 427, 330, 151, 304, 108, 362, 18, 240, 243, 301, 196, 27, 272, 155, 298, 148, 374, 304, 147, 37, 174, 292, 93, 104, 111, 225, 252, 399, 348, 96, 388, 158, 341, 37, 226, 299, 470, 97, 256, 272, 357, 33, 84, 266, 215, 453, 11, 118, 327, 389, 451, 436, 68, 174, 497, 145, 121, 30, 93, 219, 158, 396, 247, 235, 221, 271, 23, 348, 180, 463, 20, 337, 115, 457, 115, 472, 458, 401, 38, 215, 383, 340, 393, 119, 383, 460, 57, 261, 489, 416, 271, 45, 118, 116, 157, 331, 330, 478, 388, 66, 139, 63, 273, 193, 4, 257, 356, 44, 357, 200, 315, 459, 102, 409, 331, 403, 260, 203, 455, 492, 67, 196, 334, 481, 414, 108, 471, 125, 375, 142, 196, 132, 487, 184, 91, 236, 355, 42, 363, 220, 179, 212, 284, 54, 70, 406, 272, 372, 409, 207, 104, 282, 136, 467, 194, 362, 24, 236, 465, 125, 478, 309, 221, 320, 97, 411, 68, 52, 11, 169, 131, 221, 16, 408, 358, 54, 232, 499, 16, 106, 248, 430, 81, 72, 123, 406, 10, 50, 455, 32, 213, 341, 253, 99, 233, 138, 409, 311, 359, 54, 160, 329, 138, 5, 84, 107, 241, 274, 346, 26, 330, 317, 412, 304, 303, 131, 217, 280, 19, 73, 261, 433, 132, 36, 328, 408, 96, 155, 412, 240, 94, 468, 71, 457, 182, 211, 175, 241, 15, 218, 300, 499, 489, 213, 444, 231, 440, 171, 114, 157, 400, 315, 99, 304, 316, 393, 340, 200, 297, 40, 105, 224, 224, 341, 300, 419, 215, 353, 455, 140, 260, 391, 176, 143, 12, 24, 257, 105, 255, 478, 123, 412, 388, 428, 221, 15, 36, 26, 80, 107, 332, 122, 267, 493, 427, 24, 23, 14, 198, 396, 157, 192, 2, 427, 283, 27, 81, 20, 319, 172, 367, 31, 229, 81, 485, 106, 66, 461, 308, 431, 27, 271, 45, 294, 268, 35, 199, 240, 239, 315, 167, 197, 53, 392, 309, 422, 314, 98, 206, 345, 83, 154, 286, 360, 167, 222, 317, 273, 327, 296, 348, 334, 167, 130, 477, 297, 150, 497, 333, 81, 63, 434, 33, 454, 52, 4, 41, 72, 327, 46, 88, 44, 456, 394, 405, 98, 367, 272, 119, 283, 204, 137, 140, 489, 147, 450, 24, 418, 233, 222, 93, 315, 442, 27, 320, 241, 404, 315, 70, 388, 134, 309, 440, 341, 52, 425, 225, 108, 405, 297, 4, 295, 401, 395, 342, 322, 437, 213, 485, 211, 309, 149, 91, 138, 203, 451 };
		final int numClasses = 420;
		ArrayList<NumericElement<Integer>> input = new ArrayList<NumericElement<Integer>>(inputArray.length);
		for (int i = 0; i < inputArray.length; ++i) {
			input.add(new NumericElement<Integer>(inputArray[i]));
		}
		ArrayList<NumericElement<Integer>> copy = (ArrayList<NumericElement<Integer>>) input.clone();
		final int[] classBounds = CyclePartitioner.partition(input, new FlashSortPartitionFunction<NumericElement<Integer>, Integer>(input, numClasses));
		checkInput(input, classBounds, copy, numClasses);
	}

	private void checkInput(ArrayList<NumericElement<Integer>> output, int[] classBounds, ArrayList<NumericElement<Integer>> origArray, int numOrigClasses) {
		if (classBounds == null) {
			printInputCode(origArray, numOrigClasses);
		}
		assertNotNull(classBounds);

		// Confirm the max value of a class is less than the min value of the next upper class.
		int[] prevMinAndMax = getMinAndMax(output, classBounds, 0);
		int[] currMinAndMax = null;

		for (int i = 1; i < classBounds.length; ++i) {
			currMinAndMax = getMinAndMax(output, classBounds, i);
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
				int startIndex = (i == 1) ? 0 : classBounds[i - 2] + 1;
				int endIndex = classBounds[i - 1];
				for (int indexInRange = startIndex; indexInRange < endIndex; ++indexInRange) {
					if (indexInRange == classBounds[i]) {
						System.out.print("[*] ");
					}
					System.out.print("[" + indexInRange + ": " + output.get(indexInRange) + "], ");
				}
				System.out.println("[**] [" + endIndex + ", " + output.get(endIndex) + "]");
				printInputCode(origArray, numOrigClasses);
			}
			assertTrue("The length of a class's (" + i + ") range (" + range + ") should never be negative. [" + classBounds[i - 1] + ", " + classBounds[i] + "]", range >= 0);
		}
	}

	private int[] getMinAndMax(ArrayList<NumericElement<Integer>> input, int[] classBounds, int classification) {
		int[] minAndMax = new int[2];
		minAndMax[0] = Integer.MAX_VALUE;
		minAndMax[1] = Integer.MIN_VALUE;

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

	private ArrayList<NumericElement<Integer>> createNonRandomInput(int numElems) {
		ArrayList<NumericElement<Integer>> input = new ArrayList<NumericElement<Integer>>(numElems);
		for (int i = 0; i < numElems; ++i) {
			input.add(new NumericElement<Integer>(0));
		}
		Random random = new Random();
		for (int i = 0; i < numElems; ++i) {
			int index = random.nextInt(numElems);
			while (input.get(index % numElems).getValue() > 0) {
				++index;
			}
			input.set(index % numElems, new NumericElement<Integer>(i + 1));
		}

		return input;
	}

	private ArrayList<NumericElement<Integer>> createRandomInput(int numElems, int maxValue) {
		ArrayList<NumericElement<Integer>> input = new ArrayList<NumericElement<Integer>>(numElems);
		for (int i = 0; i < numElems; ++i) {
			input.add(new NumericElement<Integer>(0));
		}
		Random random = new Random();
		for (int i = 0; i < numElems; ++i) {
			int index = random.nextInt(numElems);
			while (input.get(index % numElems).getValue() > 0) {
				++index;
			}
			input.set(index % numElems, new NumericElement<Integer>(random.nextInt(maxValue)));
		}

		return input;
	}

	private void printInputCode(ArrayList<NumericElement<Integer>> input, int numClasses) {
		System.out.print("int[] inputArray = { ");
		for (int i = 0; i < (input.size() - 1); ++i) {
			System.out.print(input.get(i) + ", ");
		}
		System.out.println(input.get(input.size() - 1) + " };");
		System.out.println("final int numClasses = " + numClasses + ";");
		System.out.println("ArrayList<NumericElement<Integer>> input = new ArrayList<NumericElement<Integer>>(inputArray.length);");
		System.out.println("for (int i = 0; i < inputArray.length; ++i) {");
		System.out.println("\tinput.add(inputArray[i]);");
		System.out.println("}");
		System.out.println("final int[] classBounds = FlashSort.sort(input, numClasses);");
		System.out.println("checkInput(input, classBounds);");
	}

}
