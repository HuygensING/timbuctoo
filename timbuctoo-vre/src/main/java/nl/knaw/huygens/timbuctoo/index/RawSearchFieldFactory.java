package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.annotations.RawSearchField;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;

public class RawSearchFieldFactory {

  public String getRawSearchField(Class<? extends DomainEntity> type) {
    RawSearchField annotation = getAnnotation(type);

    if (annotation != null) {
      return annotation.value();
    }

    return "";
  }

  private RawSearchField getAnnotation(Class<? extends DomainEntity> type) {
    Class<?> typeToGetAnnotationFrom = type;
    for (; Entity.class.isAssignableFrom(typeToGetAnnotationFrom);) {
      RawSearchField annotation = typeToGetAnnotationFrom.getAnnotation(RawSearchField.class);
      if (annotation != null) {
        return annotation;
      }
      typeToGetAnnotationFrom = typeToGetAnnotationFrom.getSuperclass();
    }

    return null;
  }
}
