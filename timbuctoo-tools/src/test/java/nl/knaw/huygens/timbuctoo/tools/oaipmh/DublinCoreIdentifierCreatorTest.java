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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import nl.knaw.huygens.timbuctoo.tools.oaipmh.projecta.ProjectATestDomainEntity;

import org.junit.Test;

import com.google.common.collect.Lists;

public class DublinCoreIdentifierCreatorTest {

  private static final String BASE_URL = "http://test.com";

  @Test
  public void createCreatesAURLStringFromTheVREBaseURLTheBaseCollectionAndTheIdOfTheEntity() {
    // setup
    String id = "id12343214";
    String collection = "testdomainentitys";
    ProjectATestDomainEntity domainEntity = new ProjectATestDomainEntity();
    domainEntity.setId(id);

    DublinCoreIdentifierCreator instance = new DublinCoreIdentifierCreator();

    // action
    String identifier = instance.create(domainEntity, BASE_URL);

    // verify
    assertThat(identifier, stringContainsInOrder(Lists.newArrayList(BASE_URL, "/", collection, "/", id)));
  }
}
