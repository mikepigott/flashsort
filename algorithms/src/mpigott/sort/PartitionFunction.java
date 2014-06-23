package mpigott.sort;

/**
 * Represents a partition function for use with the {@link CyclePartitioner}.
 *
 * @author  Mike Pigott
 * @version 1.0
 */
public interface PartitionFunction<T extends Element<U>, U> {

	/**
	 * The total number of classes that an item can be partitioned into.
	 */
	public int getNumClasses();

	/**
	 * Classifies the input <code>value</code>, returning its class.
	 *
	 * @param value The value to classify.
	 * @return The <code>value</code>'s class number, in
	 *         the range [0, {@link #getNumClasses()}).
	 */
	public int getClass(T value);
}
