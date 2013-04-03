package nl.knaw.huygens.repository.util;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class JAXUtils {

  public static class API {
    public String path;
    public List<String> requestTypes;
    public List<String> mediaTypes;
    public String desc;

    public API(String path, List<String> requestTypes, List<String> mediaTypes, String desc) {
      this.path = path;
      this.requestTypes = ImmutableList.copyOf(requestTypes);
      this.mediaTypes = ImmutableList.copyOf(mediaTypes);
      this.desc = desc;
    }

    public API modifyPath(String regex, String replacement) {
      String newPath = path.replaceFirst(regex, replacement);
      return new API(newPath, requestTypes, mediaTypes, desc);
    }
  }

  /**
   * Returns an API description for each HTTP method in the specified
   * class if it has a <code>Path</code> annotation, or an empty list
   * if the <code>Path</code> annotation is missing.
   */
  public static List<API> generateAPIs(Class<?> cls) {
    List<API> list = Lists.newArrayList();

    String basePath = pathValueOf(cls);
    if (!basePath.isEmpty()) {
      for (Method method : cls.getMethods()) {
        List<String> reqs = Lists.newArrayList();
        if (method.isAnnotationPresent(GET.class)) {
          reqs.add(HttpMethod.GET);
        }
        if (method.isAnnotationPresent(POST.class)) {
          reqs.add(HttpMethod.POST);
        }
        if (method.isAnnotationPresent(PUT.class)) {
          reqs.add(HttpMethod.PUT);
        }
        if (method.isAnnotationPresent(DELETE.class)) {
          reqs.add(HttpMethod.DELETE);
        }

        if (!reqs.isEmpty()) {
          String subPath = pathValueOf(method);
          String fullPath = subPath.isEmpty() ? basePath : basePath + "/" + subPath;
          fullPath = fullPath.replaceAll("\\{([^:]*):[^}]*\\}", "$1");
          list.add(new API(fullPath, reqs, mediaTypesOf(method), descriptionOf(method)));
        }
      }
    }

    return list;
  }

  /**
   * Returns the path of the annotated element,
   * or an empty string if no annotation is present.
   */
  static String pathValueOf(AnnotatedElement element) {
    Path annotation = element.getAnnotation(Path.class);
    String value = (annotation != null) ? annotation.value() : "";
    return StringUtils.removeStart(value, "/");
  }

  static List<String> mediaTypesOf(Method method) {
    Produces annotation = method.getAnnotation(Produces.class);
    if (annotation != null) {
      return Lists.newArrayList(annotation.value());
    } else {
      return Collections.emptyList();
    }
  }

  static String descriptionOf(Method method) {
    APIDesc annotation = method.getAnnotation(APIDesc.class);
    return (annotation != null) ? annotation.value() : "";
  }

}
