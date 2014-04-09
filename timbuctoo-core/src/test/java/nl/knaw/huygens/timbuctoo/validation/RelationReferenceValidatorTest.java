package nl.knaw.huygens.timbuctoo.validation;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.util.RelationBuilder;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
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
  private StorageManager storage;
  private RelationReferenceValidator validator;

  @Before
  public void setup() {
    typeRegistryMock = mock(TypeRegistry.class);
    storage = mock(StorageManager.class);
    validator = new RelationReferenceValidator(typeRegistryMock, storage);

    relation = RelationBuilder.createRelation(Relation.class) //
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
    when(storage.getEntity(sourceType, sourceId)).thenReturn(new SourceType());
    when(storage.getEntity(targetType, targetId)).thenReturn(new TargetType());

    // action
    validator.validate(relation);

    // verify
    InOrder inOrder = Mockito.inOrder(typeRegistryMock, storage);
    inOrder.verify(typeRegistryMock).getTypeForIName(sourceTypeString);
    inOrder.verify(storage).getEntity(sourceType, sourceId);
    inOrder.verify(typeRegistryMock).getTypeForIName(targetTypeString);
    inOrder.verify(storage).getEntity(targetType, targetId);
  }

  @Test(expected = ValidationException.class)
  public void testValidateSourceDoesNotExist() throws Exception {
    when(storage.getEntity(sourceType, sourceId)).thenReturn(null);
    when(storage.getEntity(targetType, targetId)).thenReturn(new TargetType());
    validator.validate(relation);
  }

  @Test(expected = ValidationException.class)
  public void testValidateTargetDoesNotExist() throws Exception {
    when(storage.getEntity(sourceType, sourceId)).thenReturn(new SourceType());
    when(storage.getEntity(targetType, targetId)).thenReturn(null);
    validator.validate(relation);
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
