/*  */
package pex;

/**
 * Class for describing syntactic tree leaves for holding simple (atomic) types.
 * This is a template class that will be instantiated by the various classes for
 * holding specific leaves.
 * 
 * @param <StoredType>
 *          the value type
 */
@SuppressWarnings("nls")
public abstract class Value<StoredType> extends Expression {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608272140L;

  /** Stored value. */
  StoredType _value;

  /**
   * @param value
   */
  protected Value(StoredType value) {
    _value = value;
  }

  /**
   * @return the value
   */
  public StoredType getValue() {
    return _value;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    return "" + _value;
  }

}
