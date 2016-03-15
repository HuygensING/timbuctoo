package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo VRE
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

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.util.RepositoryException;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;

public class WomenWritersVRETest extends PackageVRETest {

  @Override
  protected PackageVRE createVREWithoutReceptions() {
    return new WomenWritersVRE(VRE_ID, "description", scopeMock, indexCollectionMock, resultConverterMock, repositoryMock, relationSearcher, Lists.newArrayList(REGULAR_NAME_OF_INVERSE_MATCH, REGULAR_NAME_OF_REGULAR_MATCH));
  }

  @Test
  public void getRelationTypeNamesBetweenIsFilteredByTheRelationSearchRelations() throws RepositoryException, VREException {
    inScope(TYPE);
    inScope(OTHER_TYPE);
    String otherTypeName = TypeNames.getInternalName(OTHER_TYPE);
    String typeName = TypeNames.getInternalName(TYPE);

    RelationType relationTypeMatch1 = new RelationType();
    relationTypeMatch1.setRegularName(REGULAR_NAME_OF_REGULAR_MATCH);
    relationTypeMatch1.setTargetTypeName(otherTypeName);
    relationTypeMatch1.setSourceTypeName(typeName);

    RelationType inverseMatch = new RelationType();
    inverseMatch.setRegularName(REGULAR_NAME_OF_INVERSE_MATCH);
    inverseMatch.setInverseName(INVERSE_NAME_OF_INVERSE_MATCH);
    inverseMatch.setTargetTypeName(typeName);
    inverseMatch.setSourceTypeName(otherTypeName);

    RelationType inverseNoMatch = new RelationType();
    inverseNoMatch.setInverseName("inverse");
    inverseNoMatch.setRegularName("inverseNoMatch");
    inverseNoMatch.setTargetTypeName(typeName);
    inverseNoMatch.setSourceTypeName(otherTypeName);

    RelationType regularNoMatch = new RelationType();
    regularNoMatch.setRegularName("regularNoMatch");
    regularNoMatch.setTargetTypeName(otherTypeName);
    regularNoMatch.setSourceTypeName(typeName);

    when(repositoryMock.getRelationTypes(TYPE, OTHER_TYPE)).thenReturn(Lists.newArrayList(relationTypeMatch1, inverseNoMatch, inverseMatch).iterator());

    // action
    List<String> relationTypeNamesBetween = vre.getRelationTypeNamesBetween(TYPE, OTHER_TYPE);

    // verify
    assertThat(relationTypeNamesBetween, containsInAnyOrder(REGULAR_NAME_OF_REGULAR_MATCH, INVERSE_NAME_OF_INVERSE_MATCH));
  }
}
