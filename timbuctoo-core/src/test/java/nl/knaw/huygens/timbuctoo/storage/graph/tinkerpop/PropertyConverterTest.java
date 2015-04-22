package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

public interface PropertyConverterTest {

  void setValueOfVertexSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception;

  void setValueOfVertexDoesNotSetIfTheValueIsNull() throws Exception;

  void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception;

  void setValueOfVertexThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception;

}