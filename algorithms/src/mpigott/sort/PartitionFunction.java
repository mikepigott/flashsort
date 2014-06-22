package mpigott.sort;

/**
 * Represents a partition function for cycle partitioning.
 *
 * @author  Mike Pigott
 * @version 1.0
 */
public interface PartitionFunction<T extends Element<U>, U> {

	public int getNumClasses();

	public int getClass(T value);
}
