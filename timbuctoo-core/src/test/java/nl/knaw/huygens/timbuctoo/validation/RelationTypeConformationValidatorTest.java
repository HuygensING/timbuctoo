package nl.knaw.huygens.timbuctoo.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static test.util.RelationBuilder.createRelation;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.Storage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class RelationTypeConformationValidatorTest {
  private String relationTypeId = "relationTypeId";
  private Relation relationMock;
  private RelationType relationType;
  private Storage storageMock;
  private RelationTypeConformationValidator instance;

  @Before
  public void setUp() {
    relationMock = createRelation()//
        .withRelationTypeId(relationTypeId)//
        .buildMock();

    relationType = new RelationType();

    storageMock = mock(Storage.class);

    instance = new RelationTypeConformationValidator(storageMock);
  }

  @Test
  public void testValidate() throws IOException, ValidationException {
    // when
    when(storageMock.getItem(RelationType.class, relationTypeId)).thenReturn(relationType);
    when(relationMock.conformsToRelationType(relationType)).thenReturn(true);

    // action
    instance.validate(relationMock);

    // verify
    InOrder inOrder = Mockito.inOrder(storageMock, relationMock);
    inOrder.verify(relationMock).getTypeId();
    inOrder.verify(storageMock).getItem(RelationType.class, relationTypeId);
    inOrder.verify(relationMock).conformsToRelationType(relationType);
  }

  @Test(expected = ValidationException.class)
  public void testValidateRelationTypeDoesNotExist() throws IOException, ValidationException {
    // when
    when(storageMock.getItem(RelationType.class, relationTypeId)).thenReturn(null);

    try {
      // action
      instance.validate(relationMock);
    } finally {
      // verify
      verify(relationMock).getTypeId();
      verify(storageMock).getItem(RelationType.class, relationTypeId);
      verifyNoMoreInteractions(relationMock);
    }
  }

  @Test(expected = ValidationException.class)
  public void testValidateRelationDoesNotConformToRelationType() throws IOException, ValidationException {
    // when
    when(storageMock.getItem(RelationType.class, relationTypeId)).thenReturn(relationType);
    when(relationMock.conformsToRelationType(relationType)).thenReturn(false);

    try {
      // action
      instance.validate(relationMock);
    } finally {
      // verify
      verify(relationMock).getTypeId();
      verify(storageMock).getItem(RelationType.class, relationTypeId);
      verify(relationMock).conformsToRelationType(relationType);
    }
  }
}
