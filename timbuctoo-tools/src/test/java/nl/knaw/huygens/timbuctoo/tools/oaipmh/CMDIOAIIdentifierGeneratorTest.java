package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import static nl.knaw.huygens.timbuctoo.tools.oaipmh.CMDIOAIIdentifierGenerator.PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.junit.Test;

public class CMDIOAIIdentifierGeneratorTest {
  private static final String VRE_ID = "e-BNM+";
  private static final String SIMPLIFIED_VRE_ID = "ebnm";

  @Test
  public void createConcatenateThePrefixSimplifiedVREIdAndEntityIdWithColons() {
    // setup
    CMDIOAIIdentifierGenerator instance = new CMDIOAIIdentifierGenerator();

    DomainEntity domainEntity = new TestDomainEntity();
    String id = "testId";
    domainEntity.setId(id);

    // action
    String cmdiOAIID = instance.generate(domainEntity, VRE_ID);

    // verify
    assertThat(cmdiOAIID, is(equalTo(String.format("%s:%s:%s", PREFIX, SIMPLIFIED_VRE_ID, id))));

  }
}
