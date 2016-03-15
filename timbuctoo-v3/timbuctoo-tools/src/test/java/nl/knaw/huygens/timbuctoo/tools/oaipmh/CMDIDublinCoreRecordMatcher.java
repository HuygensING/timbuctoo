package nl.knaw.huygens.timbuctoo.tools.oaipmh;

/*
 * #%L
 * Timbuctoo tools
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

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
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

  private class MdSelfLinkMatcher extends PropertyEqualityMatcher<CMDIDublinCoreRecord, String> {
    private static final String PROPERTY_NAME = "mdSelfLink";

    private MdSelfLinkMatcher(String mdSelfLink) {
      super(PROPERTY_NAME, mdSelfLink);
    }

    @Override
    protected String getItemValue(CMDIDublinCoreRecord item) {
      return item.getMdSelfLink();
    }
  }

  private class MdProfileMatcher extends PropertyEqualityMatcher<CMDIDublinCoreRecord, String> {

    private static final String PROPERTY_NAME = "mdProfile";

    public MdProfileMatcher(String mdProfile) {
      super(PROPERTY_NAME, mdProfile);
    }

    @Override
    protected String getItemValue(CMDIDublinCoreRecord item) {
      return item.getMdProfile();
    }
  }

  private class MdCreatorMatcher extends PropertyEqualityMatcher<CMDIDublinCoreRecord, String> {
    private static final String PROPERTY_NAME = "mdCreator";

    private MdCreatorMatcher(String mdCreator) {
      super(PROPERTY_NAME, mdCreator);
    }

    @Override
    protected String getItemValue(CMDIDublinCoreRecord item) {
      return item.getMdCreator();
    }
  }

  private class MdCollectionDisplayNameMatcher extends PropertyEqualityMatcher<CMDIDublinCoreRecord, String> {
    private static final String PROPERTY_NAME = "mdCollectionDisplayName";

    private MdCollectionDisplayNameMatcher(String propertyValue) {
      super(PROPERTY_NAME, propertyValue);
    }

    @Override
    protected String getItemValue(CMDIDublinCoreRecord item) {
      return item.getMdCollectionDisplayName();
    }
  }

  private class DublinCoreRecordTitleMatcher extends PropertyEqualityMatcher<CMDIDublinCoreRecord, String> {
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
