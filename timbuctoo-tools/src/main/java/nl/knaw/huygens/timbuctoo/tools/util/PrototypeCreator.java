package nl.knaw.huygens.timbuctoo.tools.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class PrototypeCreator {
  private static final Logger LOG = LoggerFactory.getLogger(PrototypeCreator.class);

  public static void main(String[] args) throws IOException, ClassNotFoundException, ClassCastException, InstantiationException, IllegalAccessException {

    new PrototypeCreator().createProtoTypes();
  }

  public void createProtoTypes() throws IOException, ClassNotFoundException, ClassCastException, InstantiationException, IllegalAccessException {
    ClassPath classPath = ClassPath.from(this.getClass().getClassLoader());

    for (ClassInfo info : classPath.getTopLevelClassesRecursive("nl.knaw.huygens.timbuctoo.model")) {
      Class<?> type = Class.forName(info.getName());
      if (TypeRegistry.isDomainEntity(type) && !Modifier.isAbstract(type.getModifiers())) {
        LOG.info("DomainEntity found {}", info.getName());

        try {
          LOG.info("instance: \n{}", new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(createInstance(TypeRegistry.toDomainEntity(type))));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private <T> T createInstance(Class<T> type) throws InstantiationException, IllegalAccessException {
    T instance = type.newInstance();

    for (Method method : type.getMethods()) {
      try {
        setValue(method, instance);
      } catch (IllegalArgumentException e) {
        LOG.error("Illegal argument for {}.", method.getName());
      } catch (InvocationTargetException e) {
        LOG.error("invocation of {} went wrong", method.getName());
      }
    }

    for (Field field : type.getFields()) {
      try {
        setValue(field, instance);
      } catch (IllegalArgumentException e) {
        LOG.error("Illegal argument for {}.", field.getName());
      }
    }

    return instance;

  }

  private <T> void setValue(Field field, T instance) throws IllegalArgumentException, IllegalAccessException {
    if (!Modifier.isFinal(field.getModifiers())) {
      field.setAccessible(true);
      field.set(instance, generateValue(field.getType(), field.getName()));
    }
  }

  private <T> void setValue(Method method, T instance) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Class<?>[] paramTypes = method.getParameterTypes();
    if (paramTypes.length == 1 && !Collection.class.isAssignableFrom(paramTypes[0]) && !Map.class.isAssignableFrom(paramTypes[0]) && method.getName().startsWith("set")) {
      method.invoke(instance, generateValue(paramTypes[0], method.getName()));
    }
  }

  private Object generateValue(Class<?> type, String name) {
    if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
      return false;
    } else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
      return 42;
    } else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
      return 42l;
    } else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
      return Math.random() * 100;
    } else if (String.class.isAssignableFrom(type)) {
      return name;
    } else if (Class.class.isAssignableFrom(type)) {
      return type;
    } else if (Datable.class.isAssignableFrom(type)) {
      return new Datable("20130411");
    } else if (PersonName.class.isAssignableFrom(type)) {
      return createName();
    }

    try {
      return createInstance(type);
    } catch (InstantiationException e) {
      LOG.error("instantian exception for type {}", type.getSimpleName());
    } catch (IllegalAccessException e) {
      LOG.error("illegal access exception for type {}", type.getSimpleName());
    }

    LOG.warn("returning null for {} of type {}", name, type.getSimpleName());
    return null;

  }

  private PersonName createName() {
    PersonName personName = new PersonName();
    personName.addNameComponent(Type.FORENAME, "forename");
    personName.addNameComponent(Type.SURNAME, "surname");

    return personName;
  }
}
