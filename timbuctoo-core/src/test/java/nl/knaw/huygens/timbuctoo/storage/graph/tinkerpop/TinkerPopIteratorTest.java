package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

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

import static nl.knaw.huygens.timbuctoo.storage.graph.TestSystemEntityWrapperBuilder.aSystemEntity;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

public class TinkerPopIteratorTest {

  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private TinkerPopIterator<TestSystemEntityWrapper, Vertex> instanceWith3Elements;
  private VertexConverter<TestSystemEntityWrapper> converter;
  private Iterator<Vertex> delegate;
  private Vertex vertex1;
  private Vertex vertex2;
  private Vertex vertex3;
  private TestSystemEntityWrapper entity1;
  private TestSystemEntityWrapper entity2;
  private TestSystemEntityWrapper entity3;
  private TinkerPopIterator<TestSystemEntityWrapper, Vertex> instanceWithoutElements;

  @Before
  public void setup() throws Exception {
    initializeVertices();
    initializeEntities();
    initializeDelegateWith3Elements();
    initializeConverter();

    instanceWith3Elements = new TinkerPopIterator<TestSystemEntityWrapper, Vertex>(converter, delegate);

    Iterator<Vertex> deletegateWithoutElements = Lists.<Vertex> newArrayList().iterator();
    instanceWithoutElements = new TinkerPopIterator<TestSystemEntityWrapper, Vertex>(converter, deletegateWithoutElements);
  }

  @SuppressWarnings("unchecked")
  private void initializeConverter() throws Exception {
    converter = mock(VertexConverter.class);
    when(converter.convertToEntity(vertex1)).thenReturn(entity1);
    when(converter.convertToEntity(vertex2)).thenReturn(entity2);
    when(converter.convertToEntity(vertex3)).thenReturn(entity3);
  }

  private void initializeDelegateWith3Elements() {
    delegate = Lists.<Vertex> newArrayList(vertex1, vertex2, vertex3).iterator();
  }

  private void initializeEntities() {
    entity1 = aSystemEntity().build();
    entity2 = aSystemEntity().build();
    entity3 = aSystemEntity().build();
  }

  private void initializeVertices() {
    vertex1 = aVertex().build();
    vertex2 = aVertex().build();
    vertex3 = aVertex().build();
  }

  @Test
  public void hasNextReturnsTrueIfTheEncapsulatedIteratorHasNext() {
    // action
    boolean hasNext = instanceWith3Elements.hasNext();

    // verify
    assertThat(hasNext, is(true));
  }

  @Test
  public void hasNextReturnsFalseIfTheEncapsulatedIteratorDoesNotHaveNext() {
    // action
    boolean hasNext = instanceWithoutElements.hasNext();

    // verify
    assertThat(hasNext, is(false));
  }

  @Test
  public void nextConvertsTheValueOfTheFirstItemOfTheEncapsulatedIterator() {
    // action
    TestSystemEntityWrapper firstElement = instanceWith3Elements.next();

    // verify
    // we know that the first element now because we use an iterator of a list as delegate.
    assertThat(firstElement, is(sameInstance(entity1)));
  }

  @Test
  public void nextReturnsNullIfTheEntityCannotBeConverted() throws Exception {
    // setup
    when(converter.convertToEntity(any(Vertex.class))).thenThrow(new ConversionException());

    // action
    TestSystemEntityWrapper element = instanceWith3Elements.next();

    // verify
    assertThat(element, is(nullValue()));
  }

  @Test(expected = NoSuchElementException.class)
  public void nextThrowsANoSuchElementExceptionIfThereAreNoMoreElements() {
    instanceWithoutElements.next();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void removeThrowsAnUnsupportedOperationException() {
    // action
    instanceWith3Elements.remove();
  }

  @Test
  public void skipIteratesUntilTheNumberOfStepsIsEqualToTheCount() {
    // action
    TestSystemEntityWrapper item = instanceWith3Elements.skip(2).next();

    // verify
    // we know that the first element now because we use an iterator of a list as delegate.
    assertThat(item, is(sameInstance(entity3)));
  }

  @Test
  public void skipIteratesUntilTheIteratorHasNoMoreElements() {
    // action
    boolean hasNext = instanceWith3Elements.skip(5).hasNext();

    // verify
    assertThat(hasNext, is(false));
  }

  @Test
  public void getSomeBuildsAListWithTheRequestNumberOfElementsThatAreLeft() {
    // action
    List<TestSystemEntityWrapper> some = instanceWith3Elements.getSome(2);

    // verify
    assertThat(some, containsInAnyOrder(entity1, entity2));
  }

  @Test
  public void getSomeReturnsAnEmptyListWhenNoItemsAreLeft() {
    // action
    List<TestSystemEntityWrapper> some = instanceWithoutElements.getSome(2);

    // verify
    assertThat(some, is(emptyCollectionOf(TYPE)));
  }

  @Test
  public void getAllReturnsAListWithAllTheElementsThatAreLeft() {
    // action
    List<TestSystemEntityWrapper> all = instanceWith3Elements.getAll();

    // verify
    assertThat(all, containsInAnyOrder(entity1, entity2, entity3));
  }

  @Test
  public void getAllReturnsAnEmtyListWhenNoItemsAreLeft() {
    // action
    List<TestSystemEntityWrapper> all = instanceWithoutElements.getAll();

    // verify
    assertThat(all, is(emptyCollectionOf(TYPE)));
  }

  @Test
  public void closeDoesNothing() {
    // action
    instanceWith3Elements.close();
  }

}
