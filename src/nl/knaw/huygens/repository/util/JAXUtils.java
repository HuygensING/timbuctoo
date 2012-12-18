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

  public static List<API> generateAPIs(Class<?> cls) {
    String basePath = getPathValue(cls);
    if (Strings.isNullOrEmpty(basePath)) {
      return Collections.<API>emptyList();
    }
    List<API> rv = Lists.newArrayList();
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
      rv.add(new API(completePath, reqs, returnTypes, desc));
    }
    return rv;
  }

  private static String getPathValue(AnnotatedElement cls) {
    Path p = cls.getAnnotation(Path.class);
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
