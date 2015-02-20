package nl.knaw.huygens.timbuctoo.storage.neo4j;

import org.junit.Test;

public interface FieldWrapperTest {
  void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception;

  void addValueToNodeDoesNotSetIfTheValueIsNull() throws Exception;

  void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception;

  void addValueToEntityDoesNothingIfThePropertyDoesNotExist() throws Exception;
}
