package nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.property;

/**
 * A FieldWrapper that wraps fields for primitives, primitive wrappers, strings, 
 * or collections of the former.  
 *
 */
class SimpleValuePropertyConverter extends AbstractPropertyConverter {

  @Override
  protected Object getFormattedValue(Object fieldValue) throws IllegalArgumentException {
    return fieldValue;
  }

  @Override
  protected Object convertValue(Object value, Class<?> fieldType) {
    return value;
  }

}
