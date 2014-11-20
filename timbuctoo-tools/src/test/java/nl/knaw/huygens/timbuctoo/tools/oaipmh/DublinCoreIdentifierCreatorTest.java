package nl.knaw.huygens.timbuctoo.tools.oaipmh;

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
