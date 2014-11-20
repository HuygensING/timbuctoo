package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import static nl.knaw.huygens.timbuctoo.tools.oaipmh.OAIRecordCreator.METADATA_PREFIXES;
import static nl.knaw.huygens.timbuctoo.tools.oaipmh.OAIRecordMatcher.likeOAIRecordWith;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import nl.knaw.huygens.oaipmh.OaiPmhRestClient;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.collect.Lists;

public class OAIRecordCreatorTest {

  private static final String OAI_URL = "http://www.example.org";
  private static final String BASE_URL = "http://www.example.com";
  private static final String VRE_ID = "e-BNM+";
  private static final ArrayList<String> SET_SPECS = Lists.newArrayList("test");
  private static final Date LAST_MODIFIED_DATE = new Date();
  private static final String METADATA = "metadata";
  private static final String IDENTIFIER = "id";
  private CMDIOAIIdentifierGenerator idGeneratorMock;
  private MetadataGenerator metadataGeneratorMock;
  private OaiPmhRestClient oaiPmhRestClientMock;
  private SetSpecGenerator setSpecGeneratorMock;
  private OAIRecordCreator instance;

  @Before
  public void setUp() {
    idGeneratorMock = mock(CMDIOAIIdentifierGenerator.class);
    metadataGeneratorMock = mock(MetadataGenerator.class);
    oaiPmhRestClientMock = mock(OaiPmhRestClient.class);
    setSpecGeneratorMock = mock(SetSpecGenerator.class);

    instance = new OAIRecordCreator(idGeneratorMock, metadataGeneratorMock, setSpecGeneratorMock, oaiPmhRestClientMock);
  }

  @Test
  public void createGeneratesAnOAIRecordAndSendsItToTheServer() {
    // setup
    TestDomainEntity testDomainEntity = createDomainEntityLastChangedOn(LAST_MODIFIED_DATE);

    // when
    when(metadataGeneratorMock.generate(testDomainEntity, IDENTIFIER, VRE_ID, BASE_URL, OAI_URL)).thenReturn(METADATA);
    when(setSpecGeneratorMock.generate(testDomainEntity, VRE_ID)).thenReturn(SET_SPECS);
    when(idGeneratorMock.generate(testDomainEntity, VRE_ID)).thenReturn(IDENTIFIER);

    // action
    instance.create(testDomainEntity, VRE_ID, BASE_URL, OAI_URL);

    // verify
    InOrder inOrder = inOrder(oaiPmhRestClientMock);
    inOrder.verify(oaiPmhRestClientMock).deleteRecord(IDENTIFIER);
    inOrder.verify(oaiPmhRestClientMock).postRecord(argThat(likeOAIRecordWith(IDENTIFIER, SET_SPECS, METADATA, METADATA_PREFIXES, LAST_MODIFIED_DATE)));
  }

  private TestDomainEntity createDomainEntityLastChangedOn(Date date) {
    TestDomainEntity testDomainEntity = new TestDomainEntity();
    Change change = new Change(date.getTime(), "", "");
    testDomainEntity.setModified(change);
    return testDomainEntity;
  }
}
