package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public class SimpleCollectionFieldWrapper<T> extends AbstractFieldWrapper {

  private final Class<T> componentType;

  public SimpleCollectionFieldWrapper(Class<T> componentType) {
    this.componentType = componentType;
  }

  @Override
  protected Object convertValue(Object value, Class<?> fieldType) {

    List<Object> list = Lists.newArrayList();

    for (int i = 0; i < Array.getLength(value); i++) {
      list.add(Array.get(value, i));
    }

    return list;
  }

  @Override
  protected boolean shouldAddValue(Object fieldValue) {

    if (fieldValue != null) {
      Collection<?> col = (Collection<?>) fieldValue;
      return !col.isEmpty();
    }

    return false;
  }

  @Override
  protected Object getFormattedValue(Object fieldValue) throws IllegalArgumentException {
    Collection<?> col = (Collection<?>) fieldValue;

    @SuppressWarnings("unchecked")
    T[] array = (T[]) Array.newInstance(componentType, col.size());

    return col.toArray(array);
  }
}
