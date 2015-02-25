package nl.knaw.huygens.timbuctoo.storage.neo4j;

public interface FieldWrapperTest {
  void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception;

  void addValueToNodeDoesNotSetIfTheValueIsNull() throws Exception;

  void addValueToNodeThrowsAConversionExceptionAnIllegalAccessExceptionIsThrown() throws Exception;

  void addValueToNodeThrowsAConversionExceptionAnIllegalArgumentExceptionIsThrown() throws Exception;

  void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception;

  void addValueToEntityDoesNothingIfThePropertyDoesNotExist() throws Exception;

  void addValueToEntityThrowsAConversionExceptionWhenAnIllegalAccessExceptionIsThrown() throws Exception;

  void addValueToEntityThrowsAConversionExceptionWhenAnIllegalArgumentExceptionIsThrown() throws Exception;
}
