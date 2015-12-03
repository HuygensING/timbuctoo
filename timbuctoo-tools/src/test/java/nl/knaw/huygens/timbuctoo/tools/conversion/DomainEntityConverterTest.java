package nl.knaw.huygens.timbuctoo.tools.conversion;

/*
 * #%L
 * Timbuctoo tools
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexDuplicator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DomainEntityConverterTest {
  private static final Object LATEST_VERTEX_ID = "latestVertexId";
  private static final String NEW_ID = "newId";
  private static final String OLD_ID = "oldId";
  private static final Class<Person> TYPE = Person.class;
  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private RevisionConverter revisionConverter;
  private int revision1;
  private int revision2;
  private List<Person> variationsOfRevision1;
  private List<Person> variationsOfRevision2;
  private AllVersionVariationMap<Person> map;
  private VertexDuplicator vertexDuplicator;
  private Vertex vertexRev1;
  private Vertex vertexRev2;
  private DomainEntityConverter<Person> instance;
  private Map<String, String> oldIdNewIdMap;
  private Map<String, Object> oldIdLatestVertexId;

  @Before
  public void setup() throws StorageException, IllegalAccessException {
    mongoStorage = mock(MongoConversionStorage.class);
    idGenerator = mock(IdGenerator.class);
    revisionConverter = mock(RevisionConverter.class);
    setupRevisionVariationMap();
    setupIdGenerator();
    setupRevisionConverter();
    setupVertexDuplicator();
    oldIdNewIdMap = Maps.newHashMap();
    oldIdLatestVertexId = Maps.newHashMap();
    instance = new DomainEntityConverter<Person>(TYPE, OLD_ID, mongoStorage, idGenerator, revisionConverter, vertexDuplicator, oldIdNewIdMap, oldIdLatestVertexId);

  }

  @SuppressWarnings("unchecked")
  private void setupRevisionVariationMap() throws StorageException {
    map = mock(AllVersionVariationMap.class);
    revision1 = 1;
    revision2 = 2;
    when(map.revisionsInOrder()).thenReturn(Lists.newArrayList(revision1, revision2));

    variationsOfRevision1 = Lists.<Person> newArrayList(new Person());
    when(map.get(revision1)).thenReturn(variationsOfRevision1);

    variationsOfRevision2 = Lists.<Person> newArrayList(new Person(), new Person());
    when(map.get(revision2)).thenReturn(variationsOfRevision2);

    when(mongoStorage.getAllVersionVariationsMapOf(TYPE, OLD_ID)).thenReturn(map);
  }

  private void setupIdGenerator() {
    when(idGenerator.nextIdFor(TYPE)).thenReturn(NEW_ID);
  }

  private void setupRevisionConverter() throws IllegalAccessException, StorageException {
    vertexRev1 = vertexWithId("vertexId1");
    when(revisionConverter.convert(OLD_ID, NEW_ID, variationsOfRevision1, revision1)).thenReturn(vertexRev1);
    vertexRev2 = vertexWithId("vertexId2");
    when(vertexRev2.getProperty(DomainEntity.DB_PID_PROP_NAME)).thenReturn("pid");
    when(revisionConverter.convert(OLD_ID, NEW_ID, variationsOfRevision2, revision2)).thenReturn(vertexRev2);
  }

  private void setupVertexDuplicator() {
    vertexDuplicator = mock(VertexDuplicator.class);
    Vertex latestVertex = vertexWithId(LATEST_VERTEX_ID);
    when(vertexDuplicator.duplicate(vertexRev2)).thenReturn(latestVertex);
  }

  private Vertex vertexWithId(Object id) {
    Vertex vertex = mock(Vertex.class);
    when(vertex.getId()).thenReturn(id);
    return vertex;
  }

  @Test
  public void convertRetrievesTheVersionsAndLetsTheVersionConverterHandleEachOne() throws Exception {
    // action
    instance.convert();

    // verify
    verifyMapContainsKeyWithValue(oldIdNewIdMap, OLD_ID, NEW_ID);
    verifyMapContainsKeyWithValue(oldIdLatestVertexId, OLD_ID, LATEST_VERTEX_ID);

    verify(revisionConverter).convert(OLD_ID, NEW_ID, variationsOfRevision1, revision1);
    verify(revisionConverter).convert(OLD_ID, NEW_ID, variationsOfRevision2, revision2);
  }

  @SuppressWarnings("unchecked")
  private <T, U> void verifyMapContainsKeyWithValue(Map<T, U> map, T key, U value) {
    assertThat(map.keySet(), contains(key));
    assertThat(map.get(key), is(value));
  }

  @Test
  public void convertLinksTheVersionsAndDuplicatesTheLatestNode() throws IllegalArgumentException, IllegalAccessException, StorageException {
    // action
    instance.convert();

    // verify
    verify(vertexRev1).addEdge(SystemRelationType.VERSION_OF.name(), vertexRev2);
    verify(vertexDuplicator).duplicate(vertexRev2);
  }

}
