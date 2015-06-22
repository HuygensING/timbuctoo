package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.storage.StorageException;

public interface EntityConversionVerifier {

  boolean isIdField(Field field);

  void verifyConversion(String oldId, String newId) throws StorageException, IllegalArgumentException, IllegalAccessException;

}