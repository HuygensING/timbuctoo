package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.util.Map;

import com.google.common.collect.Maps;

class TinkerPopQuery {

  private Map<String, Object> hasProperties;

  public TinkerPopQuery() {
    this(Maps.<String, Object> newHashMap());
  }

  public TinkerPopQuery(Map<String, Object> hasProperties) {
    this.hasProperties = hasProperties;
  }

  /**
   * Uses the property only when the value is not null.
   * @param name the name of the property
   * @param value the value of the property
   * @return the current instance
   */
  public TinkerPopQuery hasNotNullProperty(String name, Object value) {
    if (value != null) {
      hasProperties.put(name, value);
    }
    return this;
  }

}
