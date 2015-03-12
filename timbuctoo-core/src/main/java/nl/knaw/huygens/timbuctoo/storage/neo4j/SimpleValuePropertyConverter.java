package nl.knaw.huygens.timbuctoo.storage.neo4j;


/**
 * A FieldWrapper that wraps fields for primitives, primitive wrappers, strings, 
 * or collections of the former.  
 *
 */
public class SimpleValuePropertyConverter extends AbstractPropertyConverter {

  @Override
  protected Object getFormattedValue(Object fieldValue) throws IllegalArgumentException {
    return fieldValue;
  }

  @Override
  protected Object convertValue(Object value, Class<?> fieldType) {
    return value;
  }

}
