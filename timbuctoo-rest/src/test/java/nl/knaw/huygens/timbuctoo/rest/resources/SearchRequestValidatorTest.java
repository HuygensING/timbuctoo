package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.rest.util.search.SearchRequestValidator;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Test;

import test.rest.model.TestDomainEntity;
import test.rest.model.TestRelation;

import com.google.common.collect.Lists;

public class SearchRequestValidatorTest {
  private static final String INVALID_VRE_ID = "invalidVRE";
  private static final String VALID_VRE_ID = "vreID";
  private SearchRequestValidator instance;
  private SearchParametersV1 unusedSearchParametersV1 = null;
  private VREManager vreManagerMock;
  private TypeRegistry typeRegistryMock;
  private VRE vreMock;
  private RelationSearchParameters unusedRelationSearchParameters = null;
  private String nullVREId = null;
  private String unknownTypeString = "unknownType";
  private String knownTypeString = "testdomainentities";
  private String relationTypeString = "testrelations";
  private Class<? extends Relation> testRelation = TestRelation.class;
  private Repository repositoryMock;
  private String unusedTypeString;
  private String nullTypeString;

  @Before
  public void setUp() {
    vreManagerMock = mock(VREManager.class);
    typeRegistryMock = mock(TypeRegistry.class);
    vreMock = mock(VRE.class);
    when(vreManagerMock.getVREById(VALID_VRE_ID)).thenReturn(vreMock);
    when(vreManagerMock.getVREById(INVALID_VRE_ID)).thenReturn(null);

    repositoryMock = mock(Repository.class);

    instance = new SearchRequestValidator(vreManagerMock, typeRegistryMock, repositoryMock);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateNoVREIdSpecified() {
    instance.validate(nullVREId, unusedTypeString, unusedSearchParametersV1);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateVREUnknown() {
    instance.validate(INVALID_VRE_ID, unusedTypeString, unusedSearchParametersV1);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateNoTypeStringSpecified() {
    instance.validate(VALID_VRE_ID, null, unusedSearchParametersV1);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateTypeUnknown() {
    when(typeRegistryMock.getTypeForXName(unknownTypeString)).thenReturn(null);

    try {
      instance.validate(VALID_VRE_ID, unknownTypeString, unusedSearchParametersV1);
    } finally {
      verify(typeRegistryMock).getTypeForXName(unknownTypeString);
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateTypeIsNotInScope() {
    Class<TestDomainEntity> typeNotInScope = TestDomainEntity.class;
    doReturn(typeNotInScope).when(typeRegistryMock).getTypeForXName(knownTypeString);
    when(vreMock.inScope(typeNotInScope)).thenReturn(false);

    try {
      instance.validate(VALID_VRE_ID, knownTypeString, unusedSearchParametersV1);
    } finally {
      verify(typeRegistryMock).getTypeForXName(knownTypeString);
      verify(vreMock).inScope(typeNotInScope);
    }
  }

  @Test(expected = NullPointerException.class)
  public void testValidateNoSearchParametersV1Specified() {
    Class<TestDomainEntity> typeNotInScope = TestDomainEntity.class;
    doReturn(typeNotInScope).when(typeRegistryMock).getTypeForXName(knownTypeString);
    when(vreMock.inScope(typeNotInScope)).thenReturn(true);

    instance.validate(VALID_VRE_ID, knownTypeString, null);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateNoQueryStringIsSpecified() {
    SearchParametersV1 SearchParametersV1Mock = mock(SearchParametersV1.class);

    when(SearchParametersV1Mock.getTypeString()).thenReturn(knownTypeString);
    when(SearchParametersV1Mock.getTerm()).thenReturn(null);
    Class<TestDomainEntity> typeNotInScope = TestDomainEntity.class;
    doReturn(typeNotInScope).when(typeRegistryMock).getTypeForXName(knownTypeString);
    when(vreMock.inScope(typeNotInScope)).thenReturn(true);

    try {
      instance.validate(VALID_VRE_ID, knownTypeString, SearchParametersV1Mock);
    } finally {
      verify(typeRegistryMock).getTypeForXName(knownTypeString);
      verify(vreMock).inScope(typeNotInScope);
      verify(SearchParametersV1Mock).getTerm();
    }
  }

  @Test
  public void testValidateValid() {
    SearchParametersV1 SearchParametersV1Mock = mock(SearchParametersV1.class);

    when(SearchParametersV1Mock.getTypeString()).thenReturn(knownTypeString);
    when(SearchParametersV1Mock.getTerm()).thenReturn("test");
    Class<TestDomainEntity> typeNotInScope = TestDomainEntity.class;
    doReturn(typeNotInScope).when(typeRegistryMock).getTypeForXName(knownTypeString);
    when(vreMock.inScope(typeNotInScope)).thenReturn(true);

    try {
      instance.validate(VALID_VRE_ID, knownTypeString, SearchParametersV1Mock);
    } finally {
      verify(typeRegistryMock).getTypeForXName(knownTypeString);
      verify(vreMock).inScope(typeNotInScope);
      verify(SearchParametersV1Mock).getTerm();
    }
  }

  /********************************************************************************
  ** Reception Search validation **************************************************
  ********************************************************************************/

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestNoVREIdSpecified() {
    RelationSearchParameters unusedRelationSearchParameters = null;

    instance.validateRelationRequest(nullVREId, unusedTypeString, unusedRelationSearchParameters);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestUnknownVRESpecified() {
    instance.validateRelationRequest(INVALID_VRE_ID, unusedTypeString, unusedRelationSearchParameters);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestNoTypeStringSpecified() {
    instance.validateRelationRequest(VALID_VRE_ID, nullTypeString, unusedRelationSearchParameters);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestTypeStringNotValid() {
    when(typeRegistryMock.getTypeForXName(unknownTypeString)).thenReturn(null);

    try {
      instance.validateRelationRequest(VALID_VRE_ID, unknownTypeString, unusedRelationSearchParameters);
    } finally {
      verify(typeRegistryMock).getTypeForXName(unknownTypeString);
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestTypeIsNotARelation() {
    try {
      instance.validateRelationRequest(VALID_VRE_ID, knownTypeString, unusedRelationSearchParameters);
    } finally {
      verify(typeRegistryMock).getTypeForXName(knownTypeString);
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestTypeNotInScope() {
    setupTypeInScope(false, testRelation, relationTypeString);

    try {
      instance.validateRelationRequest(VALID_VRE_ID, relationTypeString, unusedRelationSearchParameters);
    } finally {
      verify(typeRegistryMock).getTypeForXName(relationTypeString);
      verify(vreMock).inScope(testRelation);
    }
  }

  @Test(expected = NullPointerException.class)
  public void testValidateRelationRequestNoSearchParametersSpecified() {
    RelationSearchParameters nullSearchParameters = null;

    setupTypeInScope(true, testRelation, relationTypeString);

    instance.validateRelationRequest(VALID_VRE_ID, relationTypeString, nullSearchParameters);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestNoSourceSearchIdSpecified() {
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setSourceSearchId(null);

    setupTypeInScope(true, testRelation, relationTypeString);

    try {
      instance.validateRelationRequest(VALID_VRE_ID, relationTypeString, relationSearchParameters);
    } finally {
      verify(typeRegistryMock).getTypeForXName(relationTypeString);
      verify(vreMock).inScope(testRelation);
    }

  }

  protected void setupTypeInScope(boolean inScope, Class<? extends DomainEntity> type, String typeString) {
    doReturn(type).when(typeRegistryMock).getTypeForXName(typeString);
    when(vreMock.inScope(type)).thenReturn(inScope);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestSourceSearchFoundForIdSpecified() {
    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    String unknownSourceSearchId = "unknowId";
    relationSearchParameters.setSourceSearchId(unknownSourceSearchId);

    setupTypeInScope(true, testRelation, relationTypeString);

    when(repositoryMock.getEntity(SearchResult.class, unknownSourceSearchId)).thenReturn(null);

    try {
      instance.validateRelationRequest(VALID_VRE_ID, relationTypeString, relationSearchParameters);
    } finally {
      verify(typeRegistryMock).getTypeForXName(relationTypeString);
      verify(vreMock).inScope(testRelation);
      verify(repositoryMock).getEntity(SearchResult.class, unknownSourceSearchId);
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestNoTargetSearchIdSpecified() {
    String knownSourceSearchId = "knowId";

    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setSourceSearchId(knownSourceSearchId);
    relationSearchParameters.setTargetSearchId(null);

    setupTypeInScope(true, testRelation, relationTypeString);

    when(repositoryMock.getEntity(SearchResult.class, knownSourceSearchId)).thenReturn(mock(SearchResult.class));

    try {
      instance.validateRelationRequest(VALID_VRE_ID, relationTypeString, relationSearchParameters);
    } finally {
      verify(typeRegistryMock).getTypeForXName(relationTypeString);
      verify(vreMock).inScope(testRelation);
      verify(repositoryMock).getEntity(SearchResult.class, knownSourceSearchId);
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestTargetSearchFoundForIdSpecified() {
    String knownSourceSearchId = "knowId";
    String unknownTargetSearchId = "unknowId";

    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setSourceSearchId(knownSourceSearchId);
    relationSearchParameters.setTargetSearchId(unknownTargetSearchId);

    setupTypeInScope(true, testRelation, relationTypeString);

    when(repositoryMock.getEntity(SearchResult.class, knownSourceSearchId)).thenReturn(mock(SearchResult.class));
    when(repositoryMock.getEntity(SearchResult.class, unknownTargetSearchId)).thenReturn(null);

    try {
      instance.validateRelationRequest(VALID_VRE_ID, relationTypeString, relationSearchParameters);
    } finally {
      verify(typeRegistryMock).getTypeForXName(relationTypeString);
      verify(vreMock).inScope(testRelation);
      verify(repositoryMock).getEntity(SearchResult.class, knownSourceSearchId);
      verify(repositoryMock).getEntity(SearchResult.class, unknownTargetSearchId);
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateRelationRequestContainsIllegalRelationTypeIdSpecified() {
    String knownSourceSearchId = "knowIdSource";
    String knownTargetSearchId = "knowIdTarget";
    String invalidRelationTypeId = "invalidId";
    String validRelationTypeId = "validId";

    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setSourceSearchId(knownSourceSearchId);
    relationSearchParameters.setTargetSearchId(knownTargetSearchId);
    relationSearchParameters.setRelationTypeIds(Lists.newArrayList(validRelationTypeId, invalidRelationTypeId));

    setupTypeInScope(true, testRelation, relationTypeString);

    when(repositoryMock.getEntity(SearchResult.class, knownSourceSearchId)).thenReturn(mock(SearchResult.class));
    when(repositoryMock.getEntity(SearchResult.class, knownTargetSearchId)).thenReturn(mock(SearchResult.class));
    when(repositoryMock.getRelationTypeById(invalidRelationTypeId)).thenReturn(null);
    when(repositoryMock.getRelationTypeById(validRelationTypeId)).thenReturn(mock(RelationType.class));

    try {
      instance.validateRelationRequest(VALID_VRE_ID, relationTypeString, relationSearchParameters);
    } finally {
      verify(typeRegistryMock).getTypeForXName(relationTypeString);
      verify(vreMock).inScope(testRelation);
      verify(repositoryMock).getEntity(SearchResult.class, knownSourceSearchId);
      verify(repositoryMock).getEntity(SearchResult.class, knownTargetSearchId);
      verify(repositoryMock).getRelationTypeById(validRelationTypeId);
      verify(repositoryMock).getRelationTypeById(invalidRelationTypeId);
    }
  }

  @Test
  public void testValidateRelationRequestValid() {
    String knownSourceSearchId = "knowIdSource";
    String knownTargetSearchId = "knowIdTarget";
    String validRelationTypeId = "validId";

    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    relationSearchParameters.setSourceSearchId(knownSourceSearchId);
    relationSearchParameters.setTargetSearchId(knownTargetSearchId);
    relationSearchParameters.setRelationTypeIds(Lists.newArrayList(validRelationTypeId));

    setupTypeInScope(true, testRelation, relationTypeString);

    when(repositoryMock.getEntity(SearchResult.class, knownSourceSearchId)).thenReturn(mock(SearchResult.class));
    when(repositoryMock.getEntity(SearchResult.class, knownTargetSearchId)).thenReturn(mock(SearchResult.class));
    when(repositoryMock.getRelationTypeById(validRelationTypeId)).thenReturn(mock(RelationType.class));

    try {
      instance.validateRelationRequest(VALID_VRE_ID, relationTypeString, relationSearchParameters);
    } finally {
      verify(typeRegistryMock).getTypeForXName(relationTypeString);
      verify(vreMock).inScope(testRelation);
      verify(repositoryMock).getEntity(SearchResult.class, knownSourceSearchId);
      verify(repositoryMock).getEntity(SearchResult.class, knownTargetSearchId);
      verify(repositoryMock).getRelationTypeById(validRelationTypeId);
    }
  }
}
