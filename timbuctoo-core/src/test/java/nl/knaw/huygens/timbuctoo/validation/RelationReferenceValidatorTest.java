package nl.knaw.huygens.timbuctoo.validation;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static test.util.RelationBuilder.createRelation;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class RelationReferenceValidatorTest {
  private String sourceId = "sourceId";
  private String sourceTypeString = "sourceType";
  private Class<SourceType> sourceType = SourceType.class;
  private String targetId = "targetId";
  private String targetTypeString = "targetType";
  private Class<TargetType> targetType = TargetType.class;
  private Relation relation;
  private TypeRegistry typeRegistryMock;
  private Storage storageMock;
  private RelationReferenceValidator relationReferenceValidator;

  @Before
  public void setUp() {
    typeRegistryMock = mock(TypeRegistry.class);
    storageMock = mock(Storage.class);
    relationReferenceValidator = new RelationReferenceValidator(typeRegistryMock, storageMock);

    relation = createRelation() //
        .withSourceId(sourceId) //
        .withSourceType(sourceTypeString) //
        .withTargetId(targetId) //
        .withTargeType(targetTypeString) //
        .build();

    setupTypeRegistry();
  }

  private void setupTypeRegistry() {
    doReturn(sourceType).when(typeRegistryMock).getTypeForIName(sourceTypeString);
    doReturn(targetType).when(typeRegistryMock).getTypeForIName(targetTypeString);
  }

  @Test
  public void testValidateIsValid() throws IOException, ValidationException {

    // when
    when(storageMock.getItem(sourceType, sourceId)).thenReturn(new SourceType());
    when(storageMock.getItem(targetType, targetId)).thenReturn(new TargetType());

    // action
    relationReferenceValidator.validate(relation);

    // verify
    InOrder inOrder = Mockito.inOrder(typeRegistryMock, storageMock);
    inOrder.verify(typeRegistryMock).getTypeForIName(sourceTypeString);
    inOrder.verify(storageMock).getItem(sourceType, sourceId);
    inOrder.verify(typeRegistryMock).getTypeForIName(targetTypeString);
    inOrder.verify(storageMock).getItem(targetType, targetId);

  }

  @Test(expected = ValidationException.class)
  public void testValidateSourceDoesNotExist() throws IOException, ValidationException {
    // when
    when(storageMock.getItem(sourceType, sourceId)).thenReturn(null);

    try {
      // action
      relationReferenceValidator.validate(relation);
    } finally {
      // verify
      verify(typeRegistryMock).getTypeForIName(sourceTypeString);
      verify(storageMock).getItem(sourceType, sourceId);
      verifyNoMoreInteractions(typeRegistryMock, storageMock);
    }
  }

  @Test(expected = ValidationException.class)
  public void testValidateTargetDoesNotExist() throws IOException, ValidationException {
    // when
    when(storageMock.getItem(sourceType, sourceId)).thenReturn(new SourceType());
    when(storageMock.getItem(targetType, targetId)).thenReturn(null);

    try {
      // action
      relationReferenceValidator.validate(relation);
    } finally {
      // verify
      verify(typeRegistryMock).getTypeForIName(sourceTypeString);
      verify(storageMock).getItem(sourceType, sourceId);
      verify(typeRegistryMock).getTypeForIName(targetTypeString);
      verify(storageMock).getItem(targetType, targetId);
    }
  }

  private static class SourceType extends DomainEntity {

    @Override
    public String getDisplayName() {
      // TODO Auto-generated method stub
      return null;
    }

  }

  private static class TargetType extends DomainEntity {

    @Override
    public String getDisplayName() {
      // TODO Auto-generated method stub
      return null;
    }

  }

}
