package nl.knaw.huygens.timbuctoo.storage.graph;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Maps;

public class TimbuctooQuery {

  private Map<String, Object> hasProperties;
  private Class<? extends Entity> type;

  public TimbuctooQuery(Class<? extends Entity> type, PropertyBusinessRules businessRules) {
    this(type, Maps.<String, Object> newHashMap());

  }

  TimbuctooQuery(Class<? extends Entity> type, Map<String, Object> hasProperties) {
    this.hasProperties = hasProperties;
    this.type = type;
  }

  /**
   * Uses the property only when the value is not null.
   * @param name the name of the property
   * @param value the value of the property
   * @return the current instance
   */
  public TimbuctooQuery hasNotNullProperty(String name, Object value) {
    if (value != null) {
      hasProperties.put(name, value);
    }
    return this;
  }

  /**
   * A method to search of a certain type
   * @param type the type to search for
   * @return the current instance
   */
  public TimbuctooQuery hasType(Class<? extends Entity> type) {
    this.type = type;
    return this;
  }

  public <T> T createGraphQuery(AbstractGraphQueryBuilder<T> queryCreator) {
    queryCreator.setHasProperties(hasProperties);

    if (type != null) {
      queryCreator.setType(type);
    }

    return queryCreator.build();

  }

}
