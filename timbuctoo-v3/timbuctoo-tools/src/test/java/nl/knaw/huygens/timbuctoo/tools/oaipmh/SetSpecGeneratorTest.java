package nl.knaw.huygens.timbuctoo.tools.oaipmh;

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

import static nl.knaw.huygens.timbuctoo.tools.oaipmh.OAISetMatcher.matchesOAISet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.oaipmh.MyOAISet;
import nl.knaw.huygens.timbuctoo.tools.oaipmh.projecta.ProjectATestDomainEntity;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class SetSpecGeneratorTest {
  private static final String SET_NAME = "ebnm testdomainentity";
  private static final ProjectATestDomainEntity DOMAIN_ENTITY = new ProjectATestDomainEntity();
  private static final String COLLECTION_SPEC = "ebnm:testdomainentity";
  private static final String SIMPLIFIED_VRE_ID = "ebnm";
  private static final String VRE_ID = "e-BNM+";
  private SetSpecGenerator instance;
  private OaiPmhRestClient oaiPmhClientMock;

  @Before
  public void setUp() {
    oaiPmhClientMock = mock(OaiPmhRestClient.class);
    instance = new SetSpecGenerator(oaiPmhClientMock);
  }

  @Test
  public void generateReturnsAListWithSetIdsThatCorrespondWithTheCurrentDomainEntityAndVRE() {
    // action
    List<String> setSpecs = instance.generate(DOMAIN_ENTITY, VRE_ID);

    //verify
    assertThat(setSpecs, containsInAnyOrder(COLLECTION_SPEC, SIMPLIFIED_VRE_ID));
  }

  @Test
  public void generateCreatesTheNewSetsOnTheOAIPMHServer() {
    // setup
    when(oaiPmhClientMock.getSets()).thenReturn(Lists.newArrayList(createOAISetWithSpec(SIMPLIFIED_VRE_ID)));

    // action
    instance.generate(DOMAIN_ENTITY, VRE_ID);

    // verify
    verify(oaiPmhClientMock).getSets();
    verify(oaiPmhClientMock).postSet(argThat(matchesOAISet()//
        .withSetSpec(COLLECTION_SPEC)//
        .withName(SET_NAME)));

    verifyNoMoreInteractions(oaiPmhClientMock);
  }

  private MyOAISet createOAISetWithSpec(String spec) {
    return new MyOAISet().setSetSpec(spec);
  }

  @Test(expected = IllegalArgumentException.class)
  public void generateThrowsAnIllegalArgumentExceptionWhenVREIdIsNull() {
    instance.generate(DOMAIN_ENTITY, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void generateThrowsAnIllegalArgumentExceptionWhenVREIdIsAnEmptyString() {
    instance.generate(DOMAIN_ENTITY, StringUtils.EMPTY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void generateThrowsAnIllegalArgumentExceptionWhenDomainEntityIsNull() {
    instance.generate(null, VRE_ID);
  }
}
