package nl.knaw.huygens.timbuctoo.storage.graph;

import java.util.Map;

import com.google.common.collect.Maps;

public class TimbuctooQuery {

  private Map<String, Object> hasProperties;
  private boolean searchByType;

  public TimbuctooQuery() {
    this(Maps.<String, Object> newHashMap());

  }

  TimbuctooQuery(Map<String, Object> hasProperties) {
    this.hasProperties = hasProperties;
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
   * @param b the type to search for
   * @return the current instance
   */
  public TimbuctooQuery setSearchByType(boolean searchByType) {
    this.searchByType = searchByType;
    return this;
  }

  public <T> T createGraphQuery(AbstractGraphQueryBuilder<T> queryCreator) throws NoSuchFieldException {
    queryCreator.setHasProperties(hasProperties);
    queryCreator.setSearchByType(searchByType);

    return queryCreator.build();

  }

}
