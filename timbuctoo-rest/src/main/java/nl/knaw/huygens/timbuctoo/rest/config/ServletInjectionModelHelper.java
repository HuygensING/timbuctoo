package nl.knaw.huygens.timbuctoo.rest.config;

public class ServletInjectionModelHelper {
  private ServletInjectionModelHelper() {}

  public static String getClassNamesString(Class<?>... classes) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Class<?> cls : classes) {
      if (!first) {
        sb.append(";");
      }
      sb.append(cls.getName());
      first = false;
    }
    return sb.toString();
  }
}
