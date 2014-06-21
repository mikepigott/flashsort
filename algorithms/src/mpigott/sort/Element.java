package mpigott.sort;

/**
 * <code>Element</code> represents an item to be sorted.  An
 * <code>Element</code> can be compared to other elements for
 * ranking, or the relative distance to another element can
 * be found.
 *
 * @author Mike Pigott
 * @version 1.0
 */
public interface Element<T> extends Comparable<Element<T>> {

	/**
	 * Returns the distance between the two numbers.  The sign of the distance
	 * is expected to be the same sign as the result of calling
	 * {@link Comparable#compareTo(Object)} with the same argument.
	 *
	 * @param other The element to determine the relative distance to.
	 * @return      The relative distance.
	 */
	public double distance(Element<T> other);

	/**
	 * The value we are wrapping.
	 *
	 * @return The internally-stored value.
	 */
	public T getValue();
}
