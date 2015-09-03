package nl.knaw.huygens.timbuctoo.util;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

public class RelationRefAdderFactoryTest {
  private RelationRefAdderFactory instance;
  private Injector injectorMock;

  @Before
  public void setUp() {
    injectorMock = mock(Injector.class);
    instance = new RelationRefAdderFactory(injectorMock);
  }

  @Test
  public void createReturnsRelationRefCreatorAnnotatedOnTheRelation() {
    verifyRelationHasRelationAdderWithRelationRefCreator(TestRelationWithRefCreatorAnnotation.class, CustomRelationRefCreator.class);
  }

  @Test
  public void createReturnsARelationRefCreatorWhenNoAnnotationIsFound() {
    verifyRelationHasRelationAdderWithRelationRefCreator(TestRelationWithoutRefCreatorAnnotation.class, DefaultRelationRefCreator.class);
  }

  private void verifyRelationHasRelationAdderWithRelationRefCreator(Class<? extends Relation> relationType, Class<? extends RelationRefCreator> relationRefCreatorType) {
    // setup
    setupInjector(relationRefCreatorType);

    // action
    RelationRefAdder relationRefAdder = instance.create(relationType);

    // verify
    assertThat(relationRefAdder, is(notNullValue(RelationRefAdder.class)));
    assertThat(relationRefAdder.getRelationRefCreator(), is(instanceOf(relationRefCreatorType)));
  }

  private <T extends RelationRefCreator> void setupInjector(Class<T> relationRefCreatorType) {
    when(injectorMock.getInstance(relationRefCreatorType)).thenReturn(mock(relationRefCreatorType));
  }
}
