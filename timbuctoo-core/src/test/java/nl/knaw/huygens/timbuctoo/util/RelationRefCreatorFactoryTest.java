package nl.knaw.huygens.timbuctoo.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.Storage;

import org.junit.Before;
import org.junit.Test;

import test.util.CustomRelationRefCreator;
import test.util.TestRelationWithRefCreatorAnnotation;
import test.util.TestRelationWithoutRefCreatorAnnotation;

public class RelationRefCreatorFactoryTest {
  private Storage storageMock;
  private TypeRegistry registryMock;
  private RelationRefCreatorFactory instance;

  @Before
  public void setUp() {
    storageMock = mock(Storage.class);
    registryMock = mock(TypeRegistry.class);

    instance = new RelationRefCreatorFactory(registryMock, storageMock);
  }

  @Test
  public void createReturnsRelationRefCreatorAnnotatedOnTheRelation() {
    verifyRelationHasRelationRefCreator(TestRelationWithRefCreatorAnnotation.class, CustomRelationRefCreator.class);
  }

  private void verifyRelationHasRelationRefCreator(Class<? extends Relation> relationType, Class<? extends RelationRefCreator> relationRefCreatorType) {
    // action
    RelationRefCreator relationRefCreator = instance.create(relationType);

    // verify
    assertThat(relationRefCreator, is(notNullValue(RelationRefCreator.class)));
    assertThat(relationRefCreator, is(instanceOf(relationRefCreatorType)));
  }

  @Test
  public void createReturnsADefaultRelationRefCreatorWhenNoAnnotationIsFound() {
    verifyRelationHasRelationRefCreator(TestRelationWithoutRefCreatorAnnotation.class, RelationRefCreator.class);
  }
}
