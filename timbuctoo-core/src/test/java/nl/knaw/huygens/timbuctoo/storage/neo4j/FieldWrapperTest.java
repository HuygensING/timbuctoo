package nl.knaw.huygens.timbuctoo.storage.neo4j;

public interface FieldWrapperTest {
  void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception;

  void addValueToNodeDoesNotSetIfTheValueIsNull() throws Exception;

  void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception;

  void addValueToEntityDoesNothingIfThePropertyDoesNotExist() throws Exception;

  void addValueToEntityThrowsAConversionExceptionWhenAIllegalAccessExceptionIsThrown() throws Exception;

  void addValueToEntityThrowsAConversionExceptionWhenAIllegalArgumentExceptionIsThrown() throws Exception;
}
