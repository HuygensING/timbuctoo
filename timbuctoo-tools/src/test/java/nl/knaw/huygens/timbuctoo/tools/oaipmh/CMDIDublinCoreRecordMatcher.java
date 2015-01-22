package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.oaipmh.metadata.CMDIDublinCoreRecord;

public class CMDIDublinCoreRecordMatcher extends CompositeMatcher<CMDIDublinCoreRecord> {

  private CMDIDublinCoreRecordMatcher() {
    super();
  }

  public static CMDIDublinCoreRecordMatcher matchesCMDIDublinCoreRecord() {

    return new CMDIDublinCoreRecordMatcher();
  }

  public CMDIDublinCoreRecordMatcher withMdSelfLink(String mdSelfLink) {
    addMatcher(new MdSelfLinkMatcher(mdSelfLink));
    return this;
  }

  public CMDIDublinCoreRecordMatcher withMdProfile(String mdProfile) {
    addMatcher(new MdProfileMatcher(mdProfile));
    return this;
  }

  public CMDIDublinCoreRecordMatcher withMdCreator(String mdCreator) {
    addMatcher(new MdCreatorMatcher(mdCreator));
    return this;
  }

  public CMDIDublinCoreRecordMatcher withMdCollectionDisplayName(String mdCollectionDisplayName) {
    addMatcher(new MdCollectionDisplayNameMatcher(mdCollectionDisplayName));
    return this;
  }

  public CMDIDublinCoreRecordMatcher withDublinCoreRecordWithTitle(String dcRecordTitle) {
    addMatcher(new DublinCoreRecordTitleMatcher(dcRecordTitle));
    return this;
  }

  private class MdSelfLinkMatcher extends PropertyMatcher<CMDIDublinCoreRecord, String> {
    private static final String PROPERTY_NAME = "mdSelfLink";

    private MdSelfLinkMatcher(String mdSelfLink) {
      super(PROPERTY_NAME, mdSelfLink);
    }

    @Override
    protected String getItemValue(CMDIDublinCoreRecord item) {
      return item.getMdSelfLink();
    }
  }

  private class MdProfileMatcher extends PropertyMatcher<CMDIDublinCoreRecord, String> {

    private static final String PROPERTY_NAME = "mdProfile";

    public MdProfileMatcher(String mdProfile) {
      super(PROPERTY_NAME, mdProfile);
    }

    @Override
    protected String getItemValue(CMDIDublinCoreRecord item) {
      return item.getMdProfile();
    }
  }

  private class MdCreatorMatcher extends PropertyMatcher<CMDIDublinCoreRecord, String> {
    private static final String PROPERTY_NAME = "mdCreator";

    private MdCreatorMatcher(String mdCreator) {
      super(PROPERTY_NAME, mdCreator);
    }

    @Override
    protected String getItemValue(CMDIDublinCoreRecord item) {
      return item.getMdCreator();
    }
  }

  private class MdCollectionDisplayNameMatcher extends PropertyMatcher<CMDIDublinCoreRecord, String> {
    private static final String PROPERTY_NAME = "mdCollectionDisplayName";

    private MdCollectionDisplayNameMatcher(String propertyValue) {
      super(PROPERTY_NAME, propertyValue);
    }

    @Override
    protected String getItemValue(CMDIDublinCoreRecord item) {
      return item.getMdCollectionDisplayName();
    }
  }

  private class DublinCoreRecordTitleMatcher extends PropertyMatcher<CMDIDublinCoreRecord, String> {
    private static final String PROPERTY_NAME = "DublinCoreRecord with title";

    public DublinCoreRecordTitleMatcher(String propertyValue) {
      super(PROPERTY_NAME, propertyValue);
    }

    @Override
    protected String getItemValue(CMDIDublinCoreRecord item) {
      return item.getDublinCoreRecord() != null ? item.getDublinCoreRecord().getTitle() : null;
    }
  }

}
