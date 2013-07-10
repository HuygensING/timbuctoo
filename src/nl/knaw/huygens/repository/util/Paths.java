package nl.knaw.huygens.repository.util;

public class Paths {

  public static String pathInUserHome(String path) {
    path = Character.toString(path.charAt(0)).equals("/") ? path : "/" + path;
    return "C:/Data/eclipse-home" + path;
    // return System.getProperty("user.home") + path;
  }

  private Paths() {
    throw new AssertionError("Non-instantiable class");
  }

}
