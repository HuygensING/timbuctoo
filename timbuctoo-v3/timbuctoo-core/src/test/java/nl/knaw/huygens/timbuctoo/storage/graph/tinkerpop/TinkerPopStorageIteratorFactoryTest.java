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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Iterator;

import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class TinkerPopStorageIteratorFactoryTest {
  private static final Class<TestSystemEntityWrapper> ENTITY_TYPE = TestSystemEntityWrapper.class;
  @SuppressWarnings("rawtypes")
  private static final Class<TinkerPopIterator> TINKERPOP_ITERATOR_TYPE = TinkerPopIterator.class;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private ElementConverterFactory elementConverterFactory;
  private TinkerPopStorageIteratorFactory instance;

  @Before
  public void setup() {
    elementConverterFactory = mock(ElementConverterFactory.class);
    instance = new TinkerPopStorageIteratorFactory(elementConverterFactory);
  }

  @Test
  public void createReturnsATinkerpopStorageIterator() throws StorageException {
    Iterator<Vertex> iterator = Lists.<Vertex> newArrayList().iterator();

    // action
    StorageIterator<TestSystemEntityWrapper> storageIterator = instance.create(ENTITY_TYPE, iterator);

    // verify
    assertThat(storageIterator, is(instanceOf(TINKERPOP_ITERATOR_TYPE)));
    verify(elementConverterFactory).forType(ENTITY_TYPE);
  }

  @Test
  public void createForRelationReturnsATinkerpopStorageIterator() {
    // setup
    Iterable<Edge> edges = Lists.newArrayList();

    // action
    StorageIterator<SubARelation> storageIterator = instance.createForRelation(RELATION_TYPE, edges.iterator());

    // verify
    assertThat(storageIterator, is(instanceOf(TINKERPOP_ITERATOR_TYPE)));
    verify(elementConverterFactory).forRelation(RELATION_TYPE);
  }
}
