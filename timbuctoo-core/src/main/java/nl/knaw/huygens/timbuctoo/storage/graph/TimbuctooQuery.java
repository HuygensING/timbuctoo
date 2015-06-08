package nl.knaw.huygens.timbuctoo.storage.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TimbuctooQuery {

  private Map<String, Object> hasProperties;
  private boolean searchByType;
  private Set<String> distinctValues;
  private boolean searchLatestOnly;
  private Class<? extends Entity> type;
  private Map<String, List<?>> inCollectionProperties;

  public TimbuctooQuery(Class<? extends Entity> type) {
    this(type, Maps.<String, Object> newHashMap(), Sets.<String> newHashSet(), Maps.<String, List<?>> newHashMap());

  }

  TimbuctooQuery(Class<? extends Entity> type, Map<String, Object> hasProperties, Set<String> distinctProperties, Map<String, List<?>> inCollectionProperties) {
    this.type = type;
    this.searchLatestOnly(true);
    this.hasProperties = hasProperties;
    this.distinctValues = distinctProperties;
    this.inCollectionProperties = inCollectionProperties;
  }

  /**
   * Uses the property only when the value is not null.
   * @param name the name of the property,
   *    if the property has a @DBProperty-annotation use that name
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
   * Method to add a filter to make sure a certain property and a value combination 
   * exists only once in a search result.  
   * @param propertyName the name of the property that should have a distinct value,
   *    if the property has a @DBProperty-annotation use that name
   * @return the current instance
   */
  public TimbuctooQuery hasDistinctValue(String propertyName) {
    this.distinctValues.add(propertyName);
    return this;
  }

  /**
   * A method to search of the type the query is created for or not. Default is false.
   * @param searchByType boolean true or false
   * @return the current instance
   */
  public TimbuctooQuery searchByType(boolean searchByType) {
    this.searchByType = searchByType;
    return this;
  }

  /**
   * Be able to search for the latest version of a type only. Default is true.
   * @param searchLatestOnly the value true or false
   * @return the current instance
   */
  public TimbuctooQuery searchLatestOnly(boolean searchLatestOnly) {
    this.searchLatestOnly = searchLatestOnly;
    return this;
  }

  public boolean searchLatestOnly() {
    return searchLatestOnly;
  }

  public <T> T createGraphQuery(AbstractGraphQueryBuilder<T> queryCreator) throws NoSuchFieldException {
    queryCreator.setHasProperties(hasProperties);
    queryCreator.setInCollectionProperties(inCollectionProperties);
    queryCreator.setSearchByType(searchByType);

    return queryCreator.build();

  }

  public void addFilterOptionsToResultFilter(ResultFilter resultFilter) {
    resultFilter.setDistinctFields(distinctValues);
    resultFilter.setType(type);
  }

  /**
   * A method that makes it possible to search on multiple values at once.
   * @param fieldName the field that should contain one of the values
   * @param allowedValues the possible values the field should contain
   * @return the current instance
   */
  public TimbuctooQuery inCollection(String fieldName, List<?> allowedValues) {
    inCollectionProperties.put(fieldName, allowedValues);
    return this;
  }

}
