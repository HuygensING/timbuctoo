package nl.knaw.huygens.timbuctoo.validation;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.util.RelationBuilder.createRelation;

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
  public void setup() {
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
  public void testValidateIsValid() throws Exception {
    // when
    when(storageMock.entityExists(sourceType, sourceId)).thenReturn(true);
    when(storageMock.entityExists(targetType, targetId)).thenReturn(true);

    // action
    relationReferenceValidator.validate(relation);

    // verify
    InOrder inOrder = Mockito.inOrder(typeRegistryMock, storageMock);
    inOrder.verify(typeRegistryMock).getTypeForIName(sourceTypeString);
    inOrder.verify(storageMock).entityExists(sourceType, sourceId);
    inOrder.verify(typeRegistryMock).getTypeForIName(targetTypeString);
    inOrder.verify(storageMock).entityExists(targetType, targetId);
  }

  @Test(expected = ValidationException.class)
  public void testValidateSourceDoesNotExist() throws Exception {
    when(storageMock.entityExists(sourceType, sourceId)).thenReturn(false);
    when(storageMock.entityExists(targetType, targetId)).thenReturn(true);
    relationReferenceValidator.validate(relation);
  }

  @Test(expected = ValidationException.class)
  public void testValidateTargetDoesNotExist() throws Exception {
    when(storageMock.entityExists(sourceType, sourceId)).thenReturn(true);
    when(storageMock.entityExists(targetType, targetId)).thenReturn(false);
    relationReferenceValidator.validate(relation);
  }

  private static class SourceType extends DomainEntity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

  private static class TargetType extends DomainEntity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

}
