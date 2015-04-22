package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;


public class ObjectValuePropertyConverter extends AbstractPropertyConverter {

  protected String getFieldName() {
    return null;
  }

  @Override
  protected Object format(Object value) {
    return value;
  }

}
