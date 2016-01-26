package nl.knaw.huygens.timbuctoo.search.description.facet;

import java.util.Map;

public class Facet {

  private final Map<String, Long> counts;
  private final String name;

  public Facet(String name, Map<String, Long> counts) {
    this.name = name;
    this.counts = counts;
  }

  public Map<String, Long> getCounts() {
    return counts;
  }

  public String getName() {
    return name;
  }
}
