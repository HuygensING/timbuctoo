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

import com.google.common.collect.Iterators;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.RelationTypes;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.TinkerPopDBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.util.DefaultRelationRefCreator;
import nl.knaw.huygens.timbuctoo.util.RelationRefAdder;
import nl.knaw.huygens.timbuctoo.util.RelationRefAdderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import test.model.derivedrelationtest.DRTDocument;
import test.model.derivedrelationtest.DRTLanguage;
import test.model.derivedrelationtest.DRTPerson;
import test.model.derivedrelationtest.DRTRelation;
import test.model.projecta.ProjectAPerson;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

public class RepositoryIntegrationTest {
  private static final Class<SearchResult> SEARCH_RESULT_TYPE = SearchResult.class;
  public static final Change CHANGE = Change.newInternalInstance();
  public static final String IS_PERSON_OF_RELATION = "isPersonOf";
  public static final String IS_LANGUAGE_OF_RELATION = "isLanguageOf";
  private Repository instance;
  private RelationRefAdderFactory relationRefCreatorFactoryMock;
  private TinkerPopDBIntegrationTestHelper dbIntegrationTestHelper;

  @Before
  public void setup() throws Exception {
    dbIntegrationTestHelper = new TinkerPopDBIntegrationTestHelper();
    dbIntegrationTestHelper.startCleanDB();

    TypeRegistry registry = TypeRegistry.getInstance();
    registry.init(Person.class.getPackage().getName() + " " + DRTPerson.class.getPackage());
    Storage storage = dbIntegrationTestHelper.createStorage(registry);
    setupRelationAdderFactory(registry, storage);

    instance = new Repository(registry, storage, relationRefCreatorFactoryMock, new RelationTypes(storage));
  }

  protected void setupRelationAdderFactory(TypeRegistry registry, Storage storage) {
    relationRefCreatorFactoryMock = mock(RelationRefAdderFactory.class);
    Mockito.when(relationRefCreatorFactoryMock.create(DRTRelation.class)).thenReturn(
      new RelationRefAdder(new DefaultRelationRefCreator(registry, storage)));
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

  @Test
  public void getEntityOrDefaultVariationWithRelationsAlsoReturnsTheDerivedRelations() throws ValidationException, StorageException {
    // add entities
    DRTDocument doc = new DRTDocument();
    String docId = instance.addDomainEntity(DRTDocument.class, doc, CHANGE);

    DRTLanguage language = new DRTLanguage();
    String languageId = instance.addDomainEntity(DRTLanguage.class, language, CHANGE);

    ProjectAPerson person = new ProjectAPerson();
    String personId = instance.addDomainEntity(ProjectAPerson.class, person, CHANGE);


    // add relation between document and language
    RelationType documentHasLanguageType = new RelationType();
    documentHasLanguageType.setRegularName("hasLanguage");
    documentHasLanguageType.setInverseName(IS_LANGUAGE_OF_RELATION);
    documentHasLanguageType.setSourceTypeName("document");
    documentHasLanguageType.setTargetTypeName("language");
    String documentHasLanguageTypeId = instance.addSystemEntity(RelationType.class, documentHasLanguageType);

    DRTRelation docLanguageRelation = new DRTRelation();
    docLanguageRelation.setSourceId(docId);
    docLanguageRelation.setSourceType("document");
    docLanguageRelation.setTargetId(languageId);
    docLanguageRelation.setTargetType("language");
    docLanguageRelation.setTypeId(documentHasLanguageTypeId);
    instance.addDomainEntity(DRTRelation.class, docLanguageRelation, CHANGE);

    // add relation between document and person
    RelationType documentHasPersonType = new RelationType();
    documentHasPersonType.setRegularName("hasPerson");
    documentHasPersonType.setInverseName(IS_PERSON_OF_RELATION);
    documentHasPersonType.setSourceTypeName("document");
    documentHasPersonType.setTargetTypeName("person");
    String documentHasPersonTypeId = instance.addSystemEntity(RelationType.class, documentHasPersonType);

    DRTRelation docPersonRelation = new DRTRelation();
    docPersonRelation.setSourceId(docId);
    docPersonRelation.setSourceType("document");
    docPersonRelation.setTargetId(personId);
    docPersonRelation.setTargetType("person");
    docPersonRelation.setTypeId(documentHasPersonTypeId);
    instance.addDomainEntity(DRTRelation.class, docPersonRelation, CHANGE);

    // add derived relation types
    RelationType personHasLanguageType = new RelationType();
    personHasLanguageType.setRegularName(DRTPerson.DERIVED_RELATION);
    personHasLanguageType.setInverseName("isLangOf");
    personHasLanguageType.setSourceTypeName("person");
    personHasLanguageType.setTargetTypeName("language");

    instance.addSystemEntity(RelationType.class, personHasLanguageType);

    RelationType langHasPersonType = new RelationType();
    langHasPersonType.setRegularName(DRTLanguage.DERIVED_RELATION);
    langHasPersonType.setInverseName("isPersonOfLang");
    langHasPersonType.setSourceTypeName("language");
    langHasPersonType.setTargetTypeName("person");

    instance.addSystemEntity(RelationType.class, langHasPersonType);


    // action
    DRTPerson personWithDerivedRelation = instance.getEntityOrDefaultVariationWithRelations(DRTPerson.class, personId);
    DRTLanguage languageWithDerivedRelation = instance.getEntityOrDefaultVariationWithRelations(DRTLanguage.class, languageId);

    // verify
    assertThat(personWithDerivedRelation.getRelations().keySet(), containsInAnyOrder(DRTPerson.DERIVED_RELATION, IS_PERSON_OF_RELATION));
    assertThat(languageWithDerivedRelation.getRelations().keySet(), containsInAnyOrder(DRTLanguage.DERIVED_RELATION, IS_LANGUAGE_OF_RELATION));
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
