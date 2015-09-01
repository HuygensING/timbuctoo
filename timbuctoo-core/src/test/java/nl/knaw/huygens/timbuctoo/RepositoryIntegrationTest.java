package nl.knaw.huygens.timbuctoo;

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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import java.util.Calendar;
import java.util.Date;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.RelationTypes;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoDBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.util.RelationRefAdderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterators;

public class RepositoryIntegrationTest {
  private static final Class<SearchResult> SEARCH_RESULT_TYPE = SearchResult.class;
  private Repository instance;
  private RelationRefAdderFactory relationRefCreatorFactoryMock;
  private MongoDBIntegrationTestHelper dbIntegrationTestHelper;

  @Before
  public void setup() throws Exception {
    dbIntegrationTestHelper = new MongoDBIntegrationTestHelper();
    dbIntegrationTestHelper.startCleanDB();

    TypeRegistry registry = TypeRegistry.getInstance();
    Storage storage = dbIntegrationTestHelper.createStorage(registry);
    relationRefCreatorFactoryMock = mock(RelationRefAdderFactory.class);

    instance = new Repository(registry, storage, relationRefCreatorFactoryMock, new RelationTypes(storage));
  }

  @After
  public void tearDown() {
    dbIntegrationTestHelper.stopDB();
  }

  @Test
  public void deleteSearchResultByDateRemovesAllSearchResultsFromTheDatabaseThatAreCreatedBeforeACertainDate() throws Exception {
    // setup
    // 2 searches made today
    addSearchResult();
    addSearchResult();

    Date tomorrow = getDateDaysFromToday(1);

    assertThatDatabaseContainsNumberOfItemsOfType(SEARCH_RESULT_TYPE, 2);

    // action
    instance.deleteSearchResultsBefore(tomorrow);

    // verify
    assertThatDatabaseContainsNumberOfItemsOfType(SEARCH_RESULT_TYPE, 0);
  }

  @Test
  public void deleteSearchResultByDateDoesNotRemoveSearchAfterTheSpecifiedDate() throws Exception {
    // setup
    // 2 searches made today
    addSearchResult();
    addSearchResult();

    Date yesterday = getDateDaysFromToday(-1);

    assertThatDatabaseContainsNumberOfItemsOfType(SEARCH_RESULT_TYPE, 2);

    // action
    instance.deleteSearchResultsBefore(yesterday);

    // verify
    assertThatDatabaseContainsNumberOfItemsOfType(SEARCH_RESULT_TYPE, 2);
  }

  private <T extends SystemEntity> void assertThatDatabaseContainsNumberOfItemsOfType(Class<T> type, int size) {
    StorageIterator<T> foundResults = instance.getSystemEntities(type);

    assertThat(foundResults, is(notNullValue()));
    assertThat(Iterators.size(foundResults), is(equalTo(size)));
  }

  private Date getDateDaysFromToday(int daysFromToday) {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, daysFromToday);
    Date date = cal.getTime();
    return date;
  }

  private void addSearchResult() throws StorageException, ValidationException {
    SearchResult searchResult = new SearchResult();

    instance.addSystemEntity(SEARCH_RESULT_TYPE, searchResult);
  }
}
