package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.lang.reflect.Array;
import java.util.Collection;

public class SimpleCollectionPropertyConverter<T> extends AbstractPropertyConverter {

  private final Class<T> componentType;

  public SimpleCollectionPropertyConverter(Class<T> componentType) {
    this.componentType = componentType;
  }

  @Override
  protected Object format(Object value) {
    Collection<?> col = (Collection<?>) value;

    @SuppressWarnings("unchecked")
    T[] array = (T[]) Array.newInstance(componentType, col.size());

    return col.toArray(array);
  }

  @Override
  protected boolean isLegalValue(Object value) {
    return value != null && !((Collection<?>) value).isEmpty();
  }

}
