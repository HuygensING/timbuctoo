package nl.knaw.huygens.repository.tools;

public class ToolBase {

  private static final String LOG4J_CONFIG = "log4j.configuration";

  static {
    if (System.getProperty(LOG4J_CONFIG) == null) {
      System.setProperty(LOG4J_CONFIG, "timbuctoo-log4j.properties");
    }
  }

}
