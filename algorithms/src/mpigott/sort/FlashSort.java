package mpigott.sort;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation of Flash Sort.
 * http://www.drdobbs.com/database/the-flashsort1-algorithm/184410496
 *
 * June 16, 2014
 *
 * Status: Broken and I don't know why.
 *
 * @author Mike Pigott
 * @version 1.0
 */
public class FlashSort {

	private static final class State {

		public State(int classCount) {
			numClasses = classCount;
			min = Integer.MAX_VALUE;
			max = Integer.MIN_VALUE;
			listSize = 0;
			classUpperBounds = null;
			currInsertIndex = null;
			cycleLeaderIndex = 0;
		}

		int min;
		int max;
		int listSize;
		int[] classUpperBounds;
		int[] currInsertIndex;
		int numClasses;
		int cycleLeaderIndex;
	}

	public static int[] sort(List<Integer> input, int numClasses) {
		if ((input == null) || input.isEmpty() || (input.size() < 2) || (numClasses < 2)) {
			return null;
		}

		State state = new State(numClasses);

		// Step 1: Determine Max, Min, and Size.
		for (int index = 0; index < input.size(); ++index) {
			Integer value = input.get(index);

			if (value == null) {
				throwInvalidInputException(index);
			}

			if (value.compareTo(state.min) < 0) {
				state.min = value.intValue();
			} else if (value.compareTo(state.max) > 0) {
				state.max = value.intValue();
			}

			++state.listSize;
		}

		if ((state.max - state.min) < state.numClasses) {
			final double min = state.min;
			final double max = state.max;
			state.numClasses = (int) (max - min + 1.0);
		}

		state.classUpperBounds = new int[state.numClasses];
		state.currInsertIndex = new int[state.numClasses];

		// Step 2: Define where each class will end.
		final int classSize = state.listSize / state.numClasses;
		for (int classNum = 1; classNum < state.numClasses; ++classNum) {
			state.classUpperBounds[classNum - 1] = (classSize * classNum - 1);
			state.currInsertIndex[classNum - 1] = (classSize * classNum - 1);
		}
		state.currInsertIndex[state.numClasses - 1] = (state.listSize - 1); // Last class goes to the end of the array.
		state.classUpperBounds[state.numClasses - 1] = (state.listSize - 1);

		// Step 3: Classify and swap until the first class is full.
		int element = 0;
		int newLocation = -1;

		while (state.cycleLeaderIndex < state.listSize) {
			// Find the next location where the element is in the wrong class.
			for (; state.cycleLeaderIndex < state.listSize; ++state.cycleLeaderIndex) {
				final int classification = classify(state, input.get(state.cycleLeaderIndex));

				final int lowerBound = getLowerBound(state, classification);

				final int upperBound = state.classUpperBounds[classification];

				if ((lowerBound > state.cycleLeaderIndex) || (upperBound < state.cycleLeaderIndex)) {
					// Found new cycle leader.
					break;
				}
			}

			if (state.cycleLeaderIndex >= state.listSize) {
				break;
			}

			element = input.get(state.cycleLeaderIndex).intValue();

			while (state.cycleLeaderIndex != newLocation) {
				final int classification = classify(state, element);

				newLocation = getNextLocation(state, classification);

				final int evicted = input.get(newLocation).intValue();

				input.set(newLocation, element);

				element = evicted;
			}

			++state.cycleLeaderIndex;
		}

		return state.classUpperBounds;
	}

	private static void throwInvalidInputException(int index) {
		throw new IllegalArgumentException("Input list cannot contain null elements.  The element at index " + index + " is null.");
	}

	private static int classify(State state, double element) {
		final double numClasses = state.numClasses;
		final double min = state.min;
		final double max = state.max;
		int classification = (int)(numClasses*(element - min)/(max - min));
		if (classification == numClasses) {
			--classification;
		}

		return classification;
	}

	private static int getLowerBound(State state, int classification) {
		return (classification == 0) ? 0 : state.classUpperBounds[classification - 1] + 1;
	}

	private static int getNextLocation(State state, int classification) {
		int location = state.currInsertIndex[classification];

		int classLowerBound = getLowerBound(state, classification);
		if (location < classLowerBound) {
			/* If the current class is full, we should re-classify in the direction of the cycle leader.
			 * This way, if our neighbors are also full, they will also re-classify towards the cycle leader,
			 * until the cycle leader is reached, ending the cycle.
			 */
			if (location < state.cycleLeaderIndex) {
				++state.classUpperBounds[classification];
				location = state.classUpperBounds[classification];
				int currClass = classification;
				int nextClass = classification + 1;
				while ((nextClass < state.numClasses) && (state.currInsertIndex[nextClass] < state.classUpperBounds[currClass])) {
					state.currInsertIndex[nextClass] = state.classUpperBounds[currClass];
					if (state.currInsertIndex[nextClass] > state.classUpperBounds[nextClass]) {
						state.classUpperBounds[nextClass] = state.currInsertIndex[nextClass];
					}
					++currClass;
					++nextClass;
				}
			} else if (location > state.cycleLeaderIndex) {
				location = state.currInsertIndex[classification];
				--state.currInsertIndex[classification];

				int currClass = classification;
				int prevClass = classification - 1;
				while ((prevClass >= 0) && (state.classUpperBounds[prevClass] > state.currInsertIndex[currClass])) {
					state.classUpperBounds[prevClass] = state.currInsertIndex[currClass];
					if (state.currInsertIndex[prevClass] > state.classUpperBounds[prevClass]) {
						state.currInsertIndex[prevClass] = state.classUpperBounds[prevClass];
					}
					--prevClass;
					--currClass;
				}
			}

		} else {
			--state.currInsertIndex[classification];
		}

		for (int rangeIter = 1; rangeIter < state.classUpperBounds.length; ++rangeIter) {
			if (state.classUpperBounds[rangeIter] < state.classUpperBounds[rangeIter - 1]) {
				throw new IllegalStateException("Upper bound of class " + rangeIter + " (" + state.classUpperBounds[rangeIter] + ") is less than upper bound of previous class " + (rangeIter - 1) + " (" + state.classUpperBounds[rangeIter - 1] + ") after defining a new location of " + location + " for class " + classification + ".");
			}
		}

		return location;
	}
}
