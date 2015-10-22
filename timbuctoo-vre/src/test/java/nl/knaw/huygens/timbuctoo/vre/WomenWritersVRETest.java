package nl.knaw.huygens.timbuctoo.vre;

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
