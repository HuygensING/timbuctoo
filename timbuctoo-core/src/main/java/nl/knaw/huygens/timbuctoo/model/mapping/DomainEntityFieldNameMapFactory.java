package nl.knaw.huygens.timbuctoo.model.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static nl.knaw.huygens.timbuctoo.storage.graph.MethodHelper.getGetterName;
import static nl.knaw.huygens.timbuctoo.storage.graph.MethodHelper.getMethodByName;

public class DomainEntityFieldNameMapFactory {
  public FieldNameMap create(Representation from, Representation to, Class<? extends DomainEntity> type) {
    FieldNameMap fieldNameMap = new FieldNameMap();

    for (Field field : type.getDeclaredFields()) {
      String key = from.getFieldName(type, field);
      String value = to.getFieldName(type, field);
      if (key != null && value != null) {
        fieldNameMap.put(key, value);
      }
    }


    return fieldNameMap;
  }

  public enum Representation {
    CLIENT {
      @Override
      protected String getFieldName(Class<? extends DomainEntity> type, Field field) {
        if(field.isAnnotationPresent(JsonProperty.class)){
          return field.getAnnotation(JsonProperty.class).value();
        }

        Method method = getMethodByName(type, getGetterName(field));
        if(isAnnotationPresentOnMethod(method, JsonProperty.class)){
          return method.getAnnotation(JsonProperty.class).value();
        }

        return field.getName();
      }


    },
    //    DATABASE, TODO implement when needed
    //    POJO,TODO implement when needed
    INDEX {
      @Override
      protected String getFieldName(Class<? extends DomainEntity> type, Field field) {
        Method method = getMethodByName(type, getGetterName(field));

        if(isAnnotationPresentOnMethod(method, IndexAnnotation.class)){
          return method.getAnnotation(IndexAnnotation.class).fieldName();
        }

        return null;
      }
    };

    protected abstract String getFieldName(Class<? extends DomainEntity> type, Field field);

    protected boolean isAnnotationPresentOnMethod(Method method, Class<? extends Annotation> annotation) {
      return method != null && method.isAnnotationPresent(annotation);
    }
  }
}
