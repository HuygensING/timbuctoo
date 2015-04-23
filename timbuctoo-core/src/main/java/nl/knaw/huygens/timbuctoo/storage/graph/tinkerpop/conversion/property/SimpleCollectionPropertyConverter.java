package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

class SimpleCollectionPropertyConverter<T> extends AbstractPropertyConverter {

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

  @Override
  protected Object convert(Object value, Class<?> fieldType) {
    List<Object> list = Lists.newArrayList();

    for (int i = 0; i < Array.getLength(value); i++) {
      list.add(Array.get(value, i));
    }

    return list;
  }

}
