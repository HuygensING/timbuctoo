package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

public class SimpleValuePropertyConverter extends AbstractPropertyConverter {

  @Override
  protected Object format(Object value) {
    return value;
  }

  @Override
  protected Object convert(Object value, Class<?> fieldType) {
    return value;
  }

}
