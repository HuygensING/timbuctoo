package nl.knaw.huygens.timbuctoo.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.DuplicateException;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.validation.RelationDuplicationValidator;

import org.junit.Before;
import org.junit.Test;

public class RelationDuplicationValidatorTest {

  private StorageManager storage;
  private RelationDuplicationValidator validator;
  private String firstId = "Id00001";
  private String secondId = "Id00002";
  private String typeId = "typeId";

  @Before
  public void setUp() {
    storage = mock(StorageManager.class);
    validator = new RelationDuplicationValidator(storage);
  }

  @Test
  public void testValidateNewValidItem() throws IOException, ValidationException {
    Relation example = createRelation(firstId, secondId, typeId);
    Relation inverseExample = createRelation(secondId, firstId, typeId);

    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    // action
    validator.validate(entityToValidate);

    // verify
    verify(storage).findEntity(Relation.class, example);
    verify(storage).findEntity(Relation.class, inverseExample);
  }

  @Test(expected = DuplicateException.class)
  public void testValidateExactSameItemExists() throws IOException, ValidationException {
    Relation example = createRelation(firstId, secondId, typeId);
    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    Relation itemFound = createRelation(firstId, secondId, typeId);
    itemFound.setSourceType("sourceType");
    itemFound.setTargetType("targetType");
    itemFound.setTypeType("typeType");

    //when
    when(storage.findEntity(Relation.class, example)).thenReturn(itemFound);

    try {
      // action
      validator.validate(itemFound);
    } finally {
      // verify
      verify(storage).findEntity(Relation.class, example);
      verifyNoMoreInteractions(storage);
    }
  }

  @Test(expected = DuplicateException.class)
  public void testValidateExactInverseItemExists() throws ValidationException, IOException {
    Relation example = createRelation(firstId, secondId, typeId);
    Relation inverseExample = createRelation(secondId, firstId, typeId);

    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    Relation itemFound = createRelation(secondId, firstId, typeId);
    itemFound.setSourceType("sourceType");
    itemFound.setTargetType("targetType");
    itemFound.setTypeType("typeType");

    // when
    when(storage.findEntity(Relation.class, inverseExample)).thenReturn(itemFound);

    try {
      // action
      validator.validate(entityToValidate);
    } finally {
      // verify
      verify(storage).findEntity(Relation.class, example);
      verify(storage).findEntity(Relation.class, inverseExample);
    }
  }

  private Relation createRelation(String sourceId, String targetId, String typeId) {
    Relation example = new Relation();
    example.setSourceId(sourceId);
    example.setTargetId(targetId);
    example.setTypeId(typeId);
    return example;
  }

}
