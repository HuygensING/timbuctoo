package nl.knaw.huygens.timbuctoo.storage.neo4j;


/**
 * A FieldWrapper that wraps fields for primitives, primitive wrappers, strings, 
 * or collections of the former.  
 *
 */
public class SimpleValueFieldWrapper extends FieldWrapper {

  @Override
  protected Object getFormattedValue(Object fieldValue) throws IllegalArgumentException {
    return fieldValue;
  }

}
