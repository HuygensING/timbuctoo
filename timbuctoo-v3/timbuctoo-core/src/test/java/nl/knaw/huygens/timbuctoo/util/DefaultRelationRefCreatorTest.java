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

import com.google.common.base.Joiner;
import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import org.junit.Before;
import org.junit.Test;
import test.variation.model.BaseVariationDomainEntity;
import test.variation.model.TestConcreteDoc;
import test.variation.model.projecta.ProjectADomainEntity;
import test.variation.model.projecta.ProjectATestDocWithPersonName;

import static nl.knaw.huygens.timbuctoo.util.RelationRefMatcher.likeRelationRef;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultRelationRefCreatorTest {
  private static final Class<TestConcreteDoc> BASE_SOURCE_TYPE = TestConcreteDoc.class;
  private static final Class<ProjectATestDocWithPersonName> SOURCE_TYPE = ProjectATestDocWithPersonName.class;
  private static final String BASE_SOURCE_TYPE_NAME = TypeNames.getInternalName(BASE_SOURCE_TYPE);
  private static final String SOURCE_TYPE_I_NAME = TypeNames.getInternalName(SOURCE_TYPE);
  private static final String SOURCE_TYPE_X_NAME = TypeNames.getExternalName(SOURCE_TYPE);
  private static final String INVERSE_RELATION_NAME = "inverseRelationName";
  private static final String SOURCE_ID = "sourceId";
  private static final Class<BaseVariationDomainEntity> BASE_TARGET_TYPE = BaseVariationDomainEntity.class;
  private static final String BASE_TARGET_I_NAME = TypeNames.getInternalName(BASE_TARGET_TYPE);
  private static final boolean ACCEPTED = true;
  private static final String RELATION_ID = "relationId";
  private static final String DISPLAY_NAME = "displayName";
  private static final int REV = 2;
  private static final String REGULAR_RELATION_NAME = "relationName";
  private static final Class<ProjectADomainEntity> TARGET_TYPE = ProjectADomainEntity.class;
  private static final String TARGET_TYPE_I_NAME = TypeNames.getInternalName(TARGET_TYPE);
  private static final String TARGET_TYPE_E_NAME = TypeNames.getExternalName(TARGET_TYPE);
  private static final String TARGET_ID = "targetId";
  private Relation relation;
  private RelationType relType;
  private EntityMapper mapper;
  private DefaultRelationRefCreator instance;
  private Storage storage;

  @Before
  public void setup() throws Exception {
    setupRelation();
    setupRelType();
    setupMapper();

    TypeRegistry typeRegistry = TypeRegistry.getInstance().init(BASE_TARGET_TYPE.getPackage().getName() + " " + TARGET_TYPE.getPackage().getName());
    storage = setupStorage();

    instance = new DefaultRelationRefCreator(typeRegistry, storage);
  }

  private void setupRelation() {
    relation = new Relation();
    relation.setId(RELATION_ID);
    relation.setAccepted(ACCEPTED);
    relation.setTargetId(TARGET_ID);
    relation.setTargetType(BASE_TARGET_I_NAME);
    relation.setRev(REV);
    relation.setSourceId(SOURCE_ID);
    relation.setSourceType(BASE_SOURCE_TYPE_NAME);
  }

  private void setupRelType() {
    relType = new RelationType();
    relType.setRegularName(REGULAR_RELATION_NAME);
    relType.setInverseName(INVERSE_RELATION_NAME);
  }

  private Storage setupStorage() throws StorageException {
    Storage storage = mock(Storage.class);
    // it should be getEntityOrDefaultVariation else the retrieval of Location fails.
    when(storage.getEntityOrDefaultVariation(TARGET_TYPE, TARGET_ID)).thenReturn(targetEntity());
    when(storage.getEntityOrDefaultVariation(SOURCE_TYPE, SOURCE_ID)).thenReturn(sourceEntity());
    return storage;
  }

  private ProjectATestDocWithPersonName sourceEntity() {
    return new ProjectATestDocWithPersonName() {
      @Override
      public String getIdentificationName() {
        return DISPLAY_NAME;
      }
    };
  }

  private ProjectADomainEntity targetEntity() {
    return new ProjectADomainEntity() {
      @Override
      public String getIdentificationName() {
        return DISPLAY_NAME;
      }
    };
  }

  private void setupMapper() {
    mapper = mock(EntityMapper.class);
    doReturn(TARGET_TYPE).when(mapper).map(BASE_TARGET_TYPE);
    doReturn(SOURCE_TYPE).when(mapper).map(BASE_SOURCE_TYPE);
  }

  @Test
  public void createRegularCreatesARelationRefToTheTargetOfTheRelation() throws Exception {
    // action
    RelationRef relationRef = instance.createRegular(mapper, relation, relType);

    // verify
    assertThat(relationRef, is(likeRelationRef() //
      .withId(TARGET_ID)//
      .withType(TARGET_TYPE_I_NAME)//
      .withPath(Joiner.on('/').join(Paths.DOMAIN_PREFIX, TARGET_TYPE_E_NAME, TARGET_ID))//
      .withRelationName(REGULAR_RELATION_NAME)//
      .withRev(REV)//
      .withDisplayName(DISPLAY_NAME)//
      .withRelationId(RELATION_ID)//
      .withAccepted(ACCEPTED)));

  }

  @Test(expected = StorageException.class)
  public void createRegularThrowsAStorageExceptionWhenTheStorageDoes() throws Exception {
    // setup
    // it should be getEntityOrDefaultVariation else the retrieval of Location fails.
    when(storage.getEntityOrDefaultVariation(TARGET_TYPE, TARGET_ID)).thenThrow(new StorageException());

    // action
    instance.createRegular(mapper, relation, relType);
  }

  @Test
  public void createInverseCreatesARelationRefToTheSourceOfTheRelation() throws Exception {
    // action
    RelationRef relationRef = instance.createInverse(mapper, relation, relType);

    // verify
    assertThat(relationRef, is(likeRelationRef() //
      .withId(SOURCE_ID)//
      .withType(SOURCE_TYPE_I_NAME)//
      .withPath(Joiner.on('/').join(Paths.DOMAIN_PREFIX, SOURCE_TYPE_X_NAME, SOURCE_ID))//
      .withRelationName(INVERSE_RELATION_NAME)//
      .withRev(REV)//
      .withDisplayName(DISPLAY_NAME)//
      .withRelationId(RELATION_ID)//
      .withAccepted(ACCEPTED)));
  }

  @Test(expected = StorageException.class)
  public void createInverseThrowsAStorageExceptionWhenTheStorageDoes() throws Exception {
    // setup
    // it should be getEntityOrDefaultVariation else the retrieval of Location fails.
    when(storage.getEntityOrDefaultVariation(SOURCE_TYPE, SOURCE_ID)).thenThrow(new StorageException());

    // action
    instance.createInverse(mapper, relation, relType);
  }
}
