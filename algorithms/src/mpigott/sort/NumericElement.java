package mpigott.sort;

/**
 * @author Mike Pigott
 *
 */
public class NumericElement<T extends Number & Comparable<T>> implements Element<T> {

	public NumericElement(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	@Override
	public int compareTo(Element<T> o) {
		return value.compareTo(o.getValue());
	}

	@Override
	public double distance(Element<T> other) {
		return value.doubleValue() - other.getValue().doubleValue();
	}

	public String toString() {
		return (value == null) ? "null" : value.toString();
	}

	private T value;
}
