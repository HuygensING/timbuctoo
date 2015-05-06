package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

public interface PropertyConverterTest {

  void setPropertyOfElementSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception;

  void setPropertyOfElementDoesNotSetIfTheValueIsNull() throws Exception;

  void setPropertyOfElementThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception;

  void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception;

  void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception;

  void addValueToEntityAddsNullWhenTheValueIsNull() throws Exception;

  void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnIllegalAccessExceptionIsThrown() throws Exception;

  void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnAnIllegalArgumentExceptionIsThrown() throws Exception;

}