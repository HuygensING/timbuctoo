package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import static nl.knaw.huygens.timbuctoo.tools.oaipmh.CMDIDublinCoreRecordCreator.MD_PROFILE;
import static nl.knaw.huygens.timbuctoo.tools.oaipmh.CMDIDublinCoreRecordMatcher.matchesCMDIDublinCoreRecord;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.oaipmh.metadata.CMDIDublinCoreRecord;
import nl.knaw.huygens.oaipmh.metadata.CMDIDublinCoreRecord.Builder;
import nl.knaw.huygens.oaipmh.metadata.DublinCoreRecord;
import nl.knaw.huygens.oaipmh.metadata.InvalidCMDIRecordException;

import org.junit.Before;
import org.junit.Test;

public class CMDIRecordCreatorTest {
  private static final String OAI_URL = "http://www.example.org";
  private static final String VRE_ID = "e-BNM+";
  private static final String DC_RECORD_TITLE = "dcRecordTitle";
  private static final String MD_COLLECTION_DISPLAY_NAME = "mdCollectionDisplayName";
  private static final String IDENTIFIER = "identifier";
  private static final String MD_SELF_LINK = String.format("%s/cmdi/%s", OAI_URL, IDENTIFIER);
  private CMDICollectionNameCreator collectionNameCreatorMock;
  private CMDIDublinCoreRecordCreator instance;
  private TestDomainEntity domainEntity;
  private DublinCoreRecord dcRecord;

  @Before
  public void setup() {
    dcRecord = createDublinCoreRecordWithTitle(DC_RECORD_TITLE);
    domainEntity = new TestDomainEntity();
    collectionNameCreatorMock = mock(CMDICollectionNameCreator.class);

    instance = new CMDIDublinCoreRecordCreator(collectionNameCreatorMock);
    //when
    when(collectionNameCreatorMock.create(domainEntity, VRE_ID)).thenReturn(MD_COLLECTION_DISPLAY_NAME);
  }

  @Test
  public void createCreatesANewCMDIRecordFromADublinCoreRecord() {
    // action
    CMDIDublinCoreRecord createdRecord = instance.create(dcRecord, domainEntity, IDENTIFIER, VRE_ID, OAI_URL);

    // verify
    assertThat(createdRecord, matchesCMDIDublinCoreRecord() //
        .withMdCollectionDisplayName(MD_COLLECTION_DISPLAY_NAME) //
        .withMdCreator(VRE_ID) //
        .withMdProfile(MD_PROFILE) //
        .withMdSelfLink(MD_SELF_LINK)//
        .withDublinCoreRecordWithTitle(DC_RECORD_TITLE));
  }

  @Test(expected = InvalidCMDIRecordException.class)
  public void createThrowsAnExceptionWhenCMDIDublicCoreRecordBuilderThrowsAnException() {
    final Builder builderMock = mock(Builder.class);

    instance = new CMDIDublinCoreRecordCreator(collectionNameCreatorMock) {
      @Override
      protected Builder newBuilder() {
        return builderMock;
      }
    };

    // when
    doThrow(InvalidCMDIRecordException.class).when(builderMock).build();

    instance.create(dcRecord, domainEntity, IDENTIFIER, VRE_ID, OAI_URL);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createThrowsAnIllegalArgumentExceptionWhenTheIdentifierIsNull() {
    instance.create(dcRecord, domainEntity, null, VRE_ID, OAI_URL);
  }

  private DublinCoreRecord createDublinCoreRecordWithTitle(String title) {
    DublinCoreRecord dcRecord = new DublinCoreRecord();
    dcRecord.setTitle(title); // at least one property should be of the dublin core record else the cmdi record will throw an exception on creation.
    return dcRecord;
  }

}
