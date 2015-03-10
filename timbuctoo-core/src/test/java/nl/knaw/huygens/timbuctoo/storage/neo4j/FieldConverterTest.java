package nl.knaw.huygens.timbuctoo.storage.neo4j;

public interface FieldConverterTest {
  void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception;

  void addValueToNodeDoesNotSetIfTheValueIsNull() throws Exception;

  void addValueToNodeThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception;

  void addValueToNodeThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception;

  void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception;

  void addValueToEntityDoesNothingIfThePropertyDoesNotExist() throws Exception;

  void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnIllegalAccessExceptionIsThrown() throws Exception;

  void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnAnIllegalArgumentExceptionIsThrown() throws Exception;

  void addValueToNodeThrowsAConversionExceptionIfGetFormatedValueThrowsAnIllegalArgumentException() throws Exception;

  void getValueReturnsTheConvertedValueOfTheNode() throws Exception;

  void getValueReturnsNullIfTheNodeDoesNotContainTheValue() throws Exception;

  void getValueThrowsAConversionExceptionIfTheValueCouldNotBeConverted() throws Exception;
}
