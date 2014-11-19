package nl.knaw.huygens.timbuctoo.tools.oaipmh;

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
