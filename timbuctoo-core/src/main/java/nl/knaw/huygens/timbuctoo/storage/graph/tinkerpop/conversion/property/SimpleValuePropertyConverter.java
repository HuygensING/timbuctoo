package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import com.tinkerpop.blueprints.Element;

class SimpleValuePropertyConverter extends AbstractPropertyConverter {

  @Override
  protected Object format(Object value) {
    return value;
  }

  @Override
  protected Object convert(Object value, Class<?> fieldType) {
    return value;
  }

  @Override
  public void removeFrom(Element element) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
