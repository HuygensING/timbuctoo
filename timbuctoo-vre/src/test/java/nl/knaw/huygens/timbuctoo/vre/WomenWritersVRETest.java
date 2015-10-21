package nl.knaw.huygens.timbuctoo.vre;

import com.google.common.collect.Lists;

public class WomenWritersVRETest extends PackageVRETest {

  @Override
  protected PackageVRE createVREWithoutReceptions() {
    return new WomenWritersVRE(VRE_ID, "description", scopeMock, indexCollectionMock, resultConverterMock, repositoryMock, relationSearcher, Lists.newArrayList());
  }

  @Override
  protected PackageVRE createVREWithRelationSearchRelations(String... receptionNames) {
    return new WomenWritersVRE(VRE_ID, "description", scopeMock, indexCollectionMock, resultConverterMock, repositoryMock, relationSearcher, Lists.newArrayList(receptionNames));
  }
}
