package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo search
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Test;

public class IndexNameCreatorTest {
  @Test
  public void testGetIndexNameFor() {

    // mock
    VRE vreMock = mock(VRE.class);

    IndexNameCreator instance = new IndexNameCreator();
    Class<? extends DomainEntity> type = ExplicitlyAnnotatedModel.class;

    // when
    when(vreMock.getScopeId()).thenReturn("scopeName");

    // action
    String indexName = instance.getIndexNameFor(vreMock, type);

    // verify
    assertThat(indexName, equalTo("scopeName.explicitlyannotatedmodel"));
  }

  @Test
  public void testGetIndexNameForSubType() {

    // mock
    VRE vreMock = mock(VRE.class);

    IndexNameCreator instance = new IndexNameCreator();
    Class<? extends DomainEntity> type = SubModel.class;

    // when
    when(vreMock.getScopeId()).thenReturn("scopeName");

    // action
    String indexName = instance.getIndexNameFor(vreMock, type);

    // verify
    assertThat(indexName, equalTo("scopeName.explicitlyannotatedmodel"));
  }
}
