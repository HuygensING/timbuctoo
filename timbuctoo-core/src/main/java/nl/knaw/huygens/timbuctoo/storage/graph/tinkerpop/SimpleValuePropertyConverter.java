package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;


public class SimpleValuePropertyConverter extends AbstractPropertyConverter {

  protected String getFieldName() {
    return null;
  }

  @Override
  protected Object format(Object value) {
    return value;
  }

  @Override
  protected Object convert(Object value) {
    return value;
  }

}
