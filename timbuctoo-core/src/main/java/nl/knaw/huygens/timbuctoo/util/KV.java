package nl.knaw.huygens.timbuctoo.util;

/**
 * Represents a key-value pair.
 */
public class KV<T> {

  private final String key;
  private final T value;

  public KV(String key, T value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public T getValue() {
    return value;
  }

}
