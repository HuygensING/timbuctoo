package nl.knaw.huygens.timbuctoo.validation;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.validation.DuplicateException;
import nl.knaw.huygens.timbuctoo.validation.RelationDuplicationValidator;

import org.junit.Before;
import org.junit.Test;

public class RelationDuplicationValidatorTest {
  private Storage storageMock;
  private RelationDuplicationValidator instance;
  private String firstId = "Id00001";
  private String secondId = "Id00002";
  private String typeId = "typeId";

  @Before
  public void setUp() {
    storageMock = mock(Storage.class);
    instance = new RelationDuplicationValidator(storageMock);
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
    instance.validate(entityToValidate);

    // verify
    verify(storageMock).findItem(Relation.class, example);
    verify(storageMock).findItem(Relation.class, inverseExample);
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
    when(storageMock.findItem(Relation.class, example)).thenReturn(itemFound);

    try {
      // action
      instance.validate(itemFound);
    } finally {
      // verify
      verify(storageMock).findItem(Relation.class, example);
      verifyNoMoreInteractions(storageMock);
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
    when(storageMock.findItem(Relation.class, inverseExample)).thenReturn(itemFound);

    try {
      // action
      instance.validate(entityToValidate);
    } finally {
      // verify
      verify(storageMock).findItem(Relation.class, example);
      verify(storageMock).findItem(Relation.class, inverseExample);
    }
  }

  @Test(expected = IOException.class)
  public void testValidateStorageThrowsAnExceptionOnExampleSearch() throws ValidationException, IOException {
    Relation example = createRelation(firstId, secondId, typeId);

    Relation entityToValidate = createRelation(firstId, secondId, typeId);
    entityToValidate.setSourceType("sourceType");
    entityToValidate.setTargetType("targetType");
    entityToValidate.setTypeType("typeType");

    Relation itemFound = createRelation(secondId, firstId, typeId);
    itemFound.setSourceType("sourceType");
    itemFound.setTargetType("targetType");
    itemFound.setTypeType("typeType");

    // when
    doThrow(IOException.class).when(storageMock).findItem(Relation.class, example);

    try {
      // action
      instance.validate(entityToValidate);
    } finally {
      // verify
      verify(storageMock).findItem(Relation.class, example);
      verifyNoMoreInteractions(storageMock);
    }
  }

  @Test(expected = IOException.class)
  public void testValidateStorageThrowsAnExceptionOnInverseExampleSearch() throws ValidationException, IOException {
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
    doThrow(IOException.class).when(storageMock).findItem(Relation.class, inverseExample);

    try {
      // action
      instance.validate(entityToValidate);
    } finally {
      // verify
      verify(storageMock).findItem(Relation.class, example);
      verify(storageMock).findItem(Relation.class, inverseExample);
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
