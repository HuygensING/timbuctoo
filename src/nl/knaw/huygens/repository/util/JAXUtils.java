package nl.knaw.huygens.repository.util;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class JAXUtils {

  public static class API {
    public String path;
    public List<String> mediaTypes;
    public List<String> requestTypes;
    public String desc;

    public API(String path, List<String> requestTypes, List<String> mediaTypes, String desc) {
      this.path = path;
      this.mediaTypes = mediaTypes;
      this.requestTypes = requestTypes;
      this.desc = desc;
    }
  }

  public static Set<Class<?>> getAllResources(Application app) {
    Set<Class<?>> classes = app.getClasses();
    return classes;
  }

  /**
   * Retutns an API description for each HTTP method in the specified
   * class if it has a <code>Path</code> annotation, or an empty list
   * if the <code>Path</code> annotation is missing.
   */
  public static List<API> generateAPIs(Class<?> cls) {
    List<API> list = Lists.newArrayList();

    String basePath = getPathValue(cls);
    if (basePath.isEmpty()) {
      return list;
    }

    Method[] methods = cls.getMethods();
    for (Method m : methods) {
      List<String> reqs = Lists.newArrayList();
      if (m.isAnnotationPresent(GET.class)) {
        reqs.add("GET");
      }
      if (m.isAnnotationPresent(POST.class)) {
        reqs.add("POST");
      }
      if (m.isAnnotationPresent(PUT.class)) {
        reqs.add("PUT");
      }
      if (m.isAnnotationPresent(DELETE.class)) {
        reqs.add("DELETE");
      }
      if (reqs.isEmpty()) {
        continue;
      }

      String subPath = getPathValue(m);
      String completePath = Strings.isNullOrEmpty(subPath) ? basePath : basePath + "/" + subPath;
      completePath = completePath.replaceAll("\\{([^:]*):[^}]*\\}", "{$1}");

      List<String> returnTypes;
      Produces p = m.getAnnotation(Produces.class);
      if (p != null) {
        returnTypes = Lists.newArrayList(p.value());
      } else {
        returnTypes = Collections.emptyList();
      }

      String desc = "";
      if (m.isAnnotationPresent(APIDesc.class)) {
        desc = m.getAnnotation(APIDesc.class).value();
      }
      list.add(new API(completePath, reqs, returnTypes, desc));
    }
    return list;
  }

  /**
   * Returns the path of the annotated element,
   * or an empty string if no annotation is present.
   */
  private static String getPathValue(AnnotatedElement element) {
    Path p = element.getAnnotation(Path.class);
    if (p == null) {
      return "";
    }
    String rv = p.value();
    if (rv.charAt(0) == '/') {
      return rv.substring(1);
    }
    return rv;
  }

}
