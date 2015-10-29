package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

/**
 * A class containing database specific properties.
 */
public final class ElementFields {
  private ElementFields() {
    throw new RuntimeException("Class is not meant to be instantiated.");
  }

  public static final String ELEMENT_TYPES = "types";
  public static final String IS_LATEST = "isLatest";
}
