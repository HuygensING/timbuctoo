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
import com.tinkerpop.blueprints.Edge;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RelationConverterTest {
  private static final int REVISION2 = 2;
  private static final int REVISION1 = 1;
  private static final Class<Relation> TYPE = Relation.class;
  private static final String OLD_ID = "id";
  private static final String NEW_ID = "newId";
  private AllVersionVariationMap<Relation> map;
  private List<Relation> variationsOfRevision1;
  private List<Relation> variationsOfRevision2;
  private MongoConversionStorage mongoStorage;
  private RelationRevisionConverter revisionConverter;
  private RelationConverter instance;
  private IdGenerator idGenerator;
  private Map<String, String> oldIdNewIdMap;
  private Edge edge1;
  private Edge edge2;

  @Before
  public void setup() throws Exception {
    oldIdNewIdMap = Maps.newHashMap();
    setupRevisionConverter();
    mongoStorage = mock(MongoConversionStorage.class);
    setupIdGenerator();

    instance = new RelationConverter(mongoStorage, revisionConverter, idGenerator, oldIdNewIdMap);

    setupRevisionVariationMap();
  }

  protected void setupRevisionConverter() throws Exception {
    revisionConverter = mock(RelationRevisionConverter.class);
    edge1 = mock(Edge.class);
    edge2 = mock(Edge.class);
    Mockito.when(revisionConverter.convert(anyString(), anyString(), Matchers.<Relation>anyList(), any(Integer.class))).thenReturn(edge1, edge2);
  }



  @SuppressWarnings("unchecked")
  private void setupRevisionVariationMap() throws StorageException {
    map = mock(AllVersionVariationMap.class);
    when(map.revisionsInOrder()).thenReturn(Lists.newArrayList(1, 2));

    variationsOfRevision1 = Lists.newArrayList(new Relation());
    when(map.get(REVISION1)).thenReturn(variationsOfRevision1);

    variationsOfRevision2 = Lists.newArrayList(new Relation(), new Relation());
    when(map.get(REVISION2)).thenReturn(variationsOfRevision2);

    when(mongoStorage.getAllVersionVariationsMapOf(TYPE, OLD_ID)).thenReturn(map);
  }

  private void setupIdGenerator() {
    idGenerator = mock(IdGenerator.class);
    when(idGenerator.nextIdFor(TYPE)).thenReturn(NEW_ID);
  }

  @Test
  public void convertRetrievesAllTheVersionsAndLetsTheRevisionConverterHandleThem() throws Exception {

    // action
    instance.convert(OLD_ID);

    // verify
    verify(revisionConverter).convert(OLD_ID, NEW_ID, variationsOfRevision1, REVISION1);
    verify(revisionConverter).convert(OLD_ID, NEW_ID, variationsOfRevision2, REVISION2);

    verifyTheRelationIdsAreMapped();
  }

  private void verifyTheRelationIdsAreMapped() {
    assertThat(oldIdNewIdMap.keySet(), contains(OLD_ID));
    assertThat(oldIdNewIdMap.get(OLD_ID), is(NEW_ID));
  }

  @Test
  public void convertSetsTheIsLatestPropertyToFalseOfEachRevisionAndSetsItToTrueForTheLatest() throws Exception {
    // action
    instance.convert(OLD_ID);

    // verify
    verify(edge1).setProperty(ElementFields.IS_LATEST, false);
    verify(edge2).setProperty(ElementFields.IS_LATEST, true);
  }

}
