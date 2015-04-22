package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

public interface PropertyConverterTest {

  void setValueOfVertexSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception;

  void setValueOfVertexDoesNotSetIfTheValueIsNull() throws Exception;

  void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception;

  void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception;

  void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception;

  void addValueToEntityAddsNullWhenTheValueIsNull() throws Exception;

  void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnIllegalAccessExceptionIsThrown() throws Exception;

  void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnAnIllegalArgumentExceptionIsThrown() throws Exception;

}