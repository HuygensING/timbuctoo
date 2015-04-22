package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;


public class NoOpPropertyConverter extends AbstractPropertyConverter {

  protected String getFieldName() {
    return null;
  }

  @Override
  protected Object format(Object value) {
    return value;
  }

}
