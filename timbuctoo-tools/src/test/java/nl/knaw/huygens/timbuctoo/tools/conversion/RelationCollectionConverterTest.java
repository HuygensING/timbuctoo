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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RelationCollectionConverterTest {
  private static final String ID1 = "id1";
  private static final String ID2 = "id2";

  @Test
  public void converLetsTheRelationVersionConverterConvertEachRelation() throws Exception {
    // setup
    RelationConverter relationConverter = mock(RelationConverter.class);
    MongoConversionStorage mongoStorage = mock(MongoConversionStorage.class);

    Relation relation1 = aRelationWithId(ID1);
    Relation relation2 = aRelationWithId(ID2);

    storageReturnsAllRelations(mongoStorage, relation1, relation2);

    RelationCollectionConverter instance = new RelationCollectionConverter(relationConverter, mongoStorage);

    // action
    instance.convert();

    // verify
    verify(relationConverter).convert(ID1);
    verify(relationConverter).convert(ID2);
  }

  private void storageReturnsAllRelations(MongoConversionStorage mongoStorage, Relation relation1, Relation relation2) throws StorageException {
    StorageIterator<Relation> iterator = StorageIteratorStub.newInstance(Lists.newArrayList(relation1, relation2));
    when(mongoStorage.getDomainEntities(Relation.class)).thenReturn(iterator);
  }

  private Relation aRelationWithId(String id) {
    Relation relation = new Relation();
    relation.setId(id);
    return relation;
  }
}
