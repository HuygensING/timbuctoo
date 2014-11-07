package nl.knaw.huygens.timbuctoo.storage.file;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.file.FileCollection;

import org.hamcrest.Matcher;

public abstract class FileCollectionTest<T extends SystemEntity> {

  protected abstract FileCollection<T> getInstance();

  protected void verifyAddReturnsAnIdAndAddsItToTheEntity(T entity, String expectedId) {
    String actualId = getInstance().add(entity);

    assertThat(actualId, is(equalTo(expectedId)));
    assertThat(entity.getId(), is(equalTo(expectedId)));
  }

  protected void verifyAddIncrementsTheId(T entity1, T entity2, T entity3, String expectedId) {
    getInstance().add(entity1);
    getInstance().add(entity2);
    String actualId = getInstance().add(entity3);

    assertThat(actualId, is(equalTo(expectedId)));
  }

  protected void verifyAddAddsTheEntityToItsCollection(T entity) {
    String id = getInstance().add(entity);
    T foundEntity = getInstance().get(id);

    assertThat(foundEntity, is(equalTo(entity)));
  }

  protected void verifyGetAllReturnsAllTheKnownEntities(Matcher<Iterable<? extends T>> matcher) {
    // action
    StorageIterator<T> entities = getInstance().getAll();

    // verify
    assertThat(entities, is(notNullValue()));
    assertThat(entities.getAll(), matcher);
  }

}
