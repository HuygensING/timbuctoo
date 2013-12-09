package nl.knaw.huygens.timbuctoo.tools.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        createEntity(TypeRegistry.toDomainEntity(type));
      }
    }
  }

  private <T extends DomainEntity> void createEntity(Class<T> type) throws InstantiationException, IllegalAccessException {
    T instance = type.newInstance();

    for (Method method : type.getMethods()) {
      try {
        setValue(method, instance);
      } catch (IllegalArgumentException e) {
        LOG.error("Illegal argument for {}.", method.getName());
        // TODO Auto-generated catch block
        //e.printStackTrace();
      } catch (InvocationTargetException e) {
        LOG.error("invocation of {} went wrong", method.getName());
        // e.printStackTrace();
      }
    }

    try {
      LOG.info("instance: {}", new ObjectMapper().writeValueAsString(instance));
    } catch (JsonProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private <T extends DomainEntity> void setValue(Method method, T instance) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Class<?>[] paramTypes = method.getParameterTypes();
    if (paramTypes.length == 1 && !Collection.class.isAssignableFrom(paramTypes[0]) && !Map.class.isAssignableFrom(paramTypes[0]) && method.getName().startsWith("set")) {
      method.invoke(instance, generateValue(paramTypes[0], method.getName()));
    }
  }

  private Object generateValue(Class<?> paramType, String methodName) {
    //    LOG.info("method name: {}", methodName);
    //    LOG.info("type: {}", paramType);

    if (Boolean.class.isAssignableFrom(paramType) || boolean.class.isAssignableFrom(paramType)) {
      //LOG.info("returning boolean");
      return false;
    } else if (Integer.class.isAssignableFrom(paramType) || int.class.isAssignableFrom(paramType)) {
      //LOG.info("returning int");
      return 42;
    } else if (Long.class.isAssignableFrom(paramType) || long.class.isAssignableFrom(paramType)) {
      return 42l;
    } else if (Double.class.isAssignableFrom(paramType) || double.class.isAssignableFrom(paramType)) {
      // LOG.info("returning double");
      return Math.random() * 100;
    } else if (String.class.isAssignableFrom(paramType)) {
      //      LOG.info("returning String");
      return methodName;
    }

    //    LOG.warn("returning null");
    return null;
  }
}
