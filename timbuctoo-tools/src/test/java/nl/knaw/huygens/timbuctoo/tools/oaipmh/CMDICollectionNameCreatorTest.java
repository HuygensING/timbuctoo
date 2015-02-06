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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import nl.knaw.huygens.timbuctoo.tools.oaipmh.projecta.ProjectATestDomainEntity;

import org.junit.Test;

public class CMDICollectionNameCreatorTest {
  @Test
  public void createJoinsTheVRENameWithNameOfThePrimiveCollectionOfTheDomainEntity() {
    // setup
    ProjectATestDomainEntity domainEntity = new ProjectATestDomainEntity();
    String vreName = "test";
    CMDICollectionNameCreator instance = new CMDICollectionNameCreator();

    // action
    String collectionName = instance.create(domainEntity, vreName);

    assertThat(collectionName, is(equalTo("test testdomainentitys")));
  }
}
