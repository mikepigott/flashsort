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
			origClassBounds = null;
			currClassBounds = null;
			cycleLeader = 0;
			cycleLeaderClass = 0;
		}

		int min;
		int max;
		int listSize;
		int[] origClassBounds;
		int[] currClassBounds;
		int numClasses;
		int cycleLeader;
		int cycleLeaderClass;
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

		state.origClassBounds = new int[state.numClasses];
		state.currClassBounds = new int[state.numClasses];

		// Step 2: Define where each class will end.
		final int classSize = state.listSize / state.numClasses;
		for (int classNum = 1; classNum < state.numClasses; ++classNum) {
			state.origClassBounds[classNum - 1] = (classSize * classNum - 1);
			state.currClassBounds[classNum - 1] = (classSize * classNum - 1);
		}
		state.currClassBounds[state.numClasses - 1] = (state.listSize - 1); // Last class goes to the end of the array.
		state.origClassBounds[state.numClasses - 1] = (state.listSize - 1);

		// Step 3: Classify and swap until the first class is full.
		int element = 0;
		int newLocation = -1;

		while (state.cycleLeader < state.listSize) {
			// Find the next location where the element is in the wrong class.
			for (; state.cycleLeader < state.listSize; ++state.cycleLeader) {
				final int classification = classify(state, input.get(state.cycleLeader));

				final int lowerBound = getLowerBound(state, classification);

				final int upperBound = state.origClassBounds[classification];

				if ((lowerBound > state.cycleLeader) || (upperBound < state.cycleLeader)) {
					// Found new cycle leader.
					state.cycleLeaderClass = getClassOfIndex(state, state.cycleLeader);
					break;
				}
			}

			if (state.cycleLeader >= state.listSize) {
				break;
			}

			element = input.get(state.cycleLeader).intValue();

			while (state.cycleLeader != newLocation) {
				final int classification = classify(state, element);

				newLocation = getNextLocation(state, classification);

				final int evicted = input.get(newLocation).intValue();

				input.set(newLocation, element);

				element = evicted;
			}

			++state.cycleLeader;
		}

		return state.origClassBounds;
	}

	private static void throwInvalidInputException(int index) {
		throw new IllegalArgumentException("Input list cannot contain null elements.  The element at index " + index + " is null.");
	}

	private static int classify(State state, double element) {
		final double numClasses = state.numClasses;
		final double min = state.min;
		final double max = state.max;

		return (int)((numClasses - 1.0)*(element - min)/(max - min));
	}

	private static int getLowerBound(State state, int classification) {
		return (classification == 0) ? 0 : state.origClassBounds[classification - 1] + 1;
	}

	private static int getNextLocation(State state, int classification) {
		int location = state.currClassBounds[classification];

		int origLowerBound = getLowerBound(state, classification);
		if (location < origLowerBound) {
			/* If the current class is full, we should re-classify in the direction of the cycle class.
			 * This way, if our neighbors are also full, they will also re-classify towards the cycle leader,
			 * until the cycle leader is reached, ending the cycle.
			 */
			if (classification < state.cycleLeaderClass) {
				++state.origClassBounds[classification];
				location = state.origClassBounds[classification];
				if (state.currClassBounds[classification + 1] < state.origClassBounds[classification]) {
					state.currClassBounds[classification + 1] = state.origClassBounds[classification];
				}
			} else if (classification > state.cycleLeaderClass) {
				location = state.currClassBounds[classification];
				--state.currClassBounds[classification];
				state.origClassBounds[classification - 1] = state.currClassBounds[classification];
			} else {
				// We're in the same class as the cycle leader; use its position.
				location = state.cycleLeader;
			}
		} else {
			--state.currClassBounds[classification];
		}

		return location;
	}

	private static int getClassOfIndex(State state, int index) {
		// Returns the index of the first element in the array greater than the key.
		int classOfPosition = Arrays.binarySearch(state.origClassBounds, index);
		if (classOfPosition < 0) {
			classOfPosition = (classOfPosition + 1) * -1;
		}
		return classOfPosition;
	}
}
