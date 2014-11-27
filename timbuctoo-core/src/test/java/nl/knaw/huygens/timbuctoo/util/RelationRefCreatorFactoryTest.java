package nl.knaw.huygens.timbuctoo.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Before;
import org.junit.Test;

import test.util.CustomRelationRefCreator;
import test.util.TestRelationWithRefCreatorAnnotation;
import test.util.TestRelationWithoutRefCreatorAnnotation;

import com.google.inject.Injector;

public class RelationRefCreatorFactoryTest {
  private RelationRefCreatorFactory instance;
  private Injector injectorMock;

  @Before
  public void setUp() {
    injectorMock = mock(Injector.class);
    instance = new RelationRefCreatorFactory(injectorMock);
  }

  @Test
  public void createReturnsRelationRefCreatorAnnotatedOnTheRelation() {
    verifyRelationHasRelationRefCreator(TestRelationWithRefCreatorAnnotation.class, CustomRelationRefCreator.class);
  }

  @Test
  public void createReturnsADefaultRelationRefCreatorWhenNoAnnotationIsFound() {
    verifyRelationHasRelationRefCreator(TestRelationWithoutRefCreatorAnnotation.class, RelationRefCreator.class);
  }

  private void verifyRelationHasRelationRefCreator(Class<? extends Relation> relationType, Class<? extends RelationRefCreator> relationRefCreatorType) {
    // setup
    setupInjector(relationRefCreatorType);

    // action
    RelationRefCreator relationRefCreator = instance.create(relationType);

    // verify
    assertThat(relationRefCreator, is(notNullValue(RelationRefCreator.class)));
    assertThat(relationRefCreator, is(instanceOf(relationRefCreatorType)));
  }

  private <T extends RelationRefCreator> void setupInjector(Class<T> relationRefCreatorType) {
    when(injectorMock.getInstance(relationRefCreatorType)).thenReturn(mock(relationRefCreatorType));
  }
}
