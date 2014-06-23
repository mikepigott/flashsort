package mpigott.sort;

import java.util.List;

/**
 * Implementation of the Flash Sort cycle-based partitioner.
 * http://www.drdobbs.com/database/the-flashsort1-algorithm/184410496
 *
 * The purpose behind a classification algorithm is that if you can evenly-
 * distribute your (large) input data set into classes, the input elements
 * can classified and moved into their respective class in O(N) time.
 *
 * The key innovation behind the partition function in Flash Sort is to
 * perform that classification and move in-place.  The flash sort algorithm
 * works by defining a "cycle leader" - the first element in the list that is
 * not in the correct class.  The cycle leader is moved to its correct class,
 * evicting an existing item - which gets moved to its class, and so on.  Once
 * the cycle leader is replaced, the cycle stops - and we search for the next
 * element in the list that is in the wrong place.
 *
 * Once all of the elements have been placed, each class can be sorted using a
 * standard sorting algorithm (insertion sort is described, but any will do).
 *
 * The challenge is in the edge cases.  If more elements go in a class than the
 * class was previously allocated for, the class needs to expand in some way.
 *
 * An existing Java implementation ( http://home.westman.wave.ca/~rhenry/sort/src/FlashSortAlgorithm.java )
 * works around this by walking the array once, calculating all of the class sizes, and
 * splitting the ranges before walking the list again, and moving the elements on the second
 * pass.
 *
 * My implementation expands the class sizes dynamically, shortening the neighboring
 * class sizes.  Cascading is also handled, so if a neighboring class is moved into
 * its neighbor's class, both are resized accordingly.  And so on.
 *
 * When we expand a class, we always expand in the direction of the cycle leader.  That
 * is the only space in the array that is guaranteed to be unfilled.  If we expand into
 * a neighbor, we evict the element on the border.  Since we always expand in the direction
 * of the cycle leader, that element will be placed on the opposite end of the neighbor's
 * class, expanding it, and so on, until we reach the cycle leader, ending the cycle.
 *
 * This implementation is also completely thread-safe, provided the same input array is not
 * used concurrently with another algorithm (or itself).  This is an in-place sort, after all.
 *
 * More information can be found in the linked Dr. Dobbs article.
 *
 * @author Mike Pigott
 * @version 1.0
 */
public class CyclePartitioner {

	/* Represents the current state of the algorithm.
	 * Passing this around allows flash sort to have
	 * no side effects, and allows the algorithm to be
	 * thread safe.
	 */
	private static final class State<T extends Element<U>, U> {

		public State(int classCount) {
			numClasses = classCount;
			listSize = 0;
			classUpperBounds = null;
			currInsertIndex = null;
			cycleLeaderIndex = 0;
		}

		int listSize;
		int[] classUpperBounds;
		int[] currInsertIndex;
		int numClasses;
		int cycleLeaderIndex;
	}

	/**
	 * Partitions the input array into the provided number of classes in O(N) time, in-place,
	 * unstably.  The upper bounds of all of the classes is returned, allowing for a
	 * separate sorting algorithm to be conducted on each class, possibly in parallel.
	 *
	 * The original Dr. Dobbs article recommended the number of classes to be (0.43 * input.size)
	 * if using serial insertion sort as the second sorting algorithm.  However, I found that a
	 * smaller number of classes is better, to be followed by an N*log(N) algorithm.  For example,
	 * with a list size of 1000 randomly-generated elements, a class size of 420 required ~38,000
	 * moves to fill each class.  However, using 40 classes only required 1,651 moves to complete.
	 * Adding in a secondary sort of 40*25*log(25), or 4644, brings the total computation to 6,295
	 * moves.  1000*log(1000), in comparison, is 9,966.
	 *
	 * If the second stage will be done in parallel, choose the number of processors / hyper-threads
	 * you have to do the work.
	 *
	 * @param input The input array to bucket into classes.
	 *
	 * @param partitionFunction The function to use when partitioning elements into classes.
	 *
	 * @return The upper bounds of each class, in increasing order, or
	 *         <code>null</code> if either the array has fewer than 2
	 *         elements, or if the partition function requests fewer
	 *         than two classes.
	 */
	public static <T extends Element<U>, U> int[] partition(List<T> input, PartitionFunction<T, U> partitionFunction) {
		if ((input == null) || input.isEmpty() || (input.size() < 2) || (partitionFunction.getNumClasses() < 2)) {
			return null;
		}

		State<T, U> state = new State<T, U>(partitionFunction.getNumClasses());

		state.listSize = input.size();
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
		T element = null;
		int newLocation = -1;

		while (state.cycleLeaderIndex < state.listSize) {
			// Find the next location where the element is in the wrong class.
			for (; state.cycleLeaderIndex < state.listSize; ++state.cycleLeaderIndex) {
				final int classification = partitionFunction.getClass(input.get(state.cycleLeaderIndex));

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

			element = input.get(state.cycleLeaderIndex);

			while (state.cycleLeaderIndex != newLocation) {
				final int classification = partitionFunction.getClass(element);

				newLocation = getNextLocation(state, classification);

				final T evicted = input.get(newLocation);

				input.set(newLocation, element);

				element = evicted;
			}

			++state.cycleLeaderIndex;
		}

		return state.classUpperBounds;
	}

	/* This determines the lower bound of any class.  This is the lowest element in the
	 * array that could be inserted into without affecting the next lower neighbor.
	 */
	private static <T extends Element<U>, U> int getLowerBound(State<T, U> state, int classification) {
		return (classification == 0) ? 0 : state.classUpperBounds[classification - 1] + 1;
	}

	/* This is the most complicated part of the algorithm: determining the next location
	 * to place the current element into.  At the start, we estimated the upper bounds of
	 * each class (state.classUpperBounds), and as we insert elements, we move that value
	 * downward (state.currInsertIndex).  If we ever try to insert into the next-lowest
	 * class, it means that the class is full, and we need to expand it.
	 *
	 * We always expand in the direction of the cycle leader.  As described in the class-
	 * level documentation, this allows us to guarantee that we will eventually reach an
	 * an unclassified position in the array (the cycle leader itself).
	 *
	 * When we expand our class, we also need to shrink the affected neighboring class.
	 * The algorithm will handle re-classifying the neighbor automatically, as we evict
	 * the nearest element in that neighbor.
	 *
	 * If that neighboring class is empty, we expand into its neighbor, and likewise we
	 * need to adjust both.  And so on down the line.
	 */
	private static <T extends Element<U>, U> int getNextLocation(State<T, U> state, int classification) {
		int location = state.currInsertIndex[classification];

		int classLowerBound = getLowerBound(state, classification);
		if (location < classLowerBound) {
			/* If the current class is full, we should re-classify in the direction of the cycle leader.
			 * This way, if our neighbors are also full, they will also re-classify towards the cycle leader,
			 * until the cycle leader is reached, ending the cycle.
			 */
			if (location >= state.cycleLeaderIndex) {
				--state.currInsertIndex[classification];

				// Shrink lower neighbor(s).
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

			} else if (location < state.cycleLeaderIndex) {
				++state.classUpperBounds[classification];
				location = state.classUpperBounds[classification];

				// Shrink upper neighbor(s).
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
			}

		} else {
			--state.currInsertIndex[classification];
		}

		return location;
	}
}
