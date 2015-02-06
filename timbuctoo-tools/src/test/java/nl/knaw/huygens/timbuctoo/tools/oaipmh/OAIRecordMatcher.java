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

import java.util.Date;
import java.util.List;

import nl.knaw.huygens.oaipmh.OAIRecord;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Objects;

public class OAIRecordMatcher extends TypeSafeMatcher<OAIRecord> {

  private final String id;
  private final List<String> setSpecs;
  private final String metadata;
  private final List<String> metadataPrefixes;
  private final Date datestamp;

  private OAIRecordMatcher(String id, List<String> setSpecs, String metadata, List<String> metadataPrefixes, Date datestamp) {
    this.id = id;
    this.setSpecs = setSpecs;
    this.metadata = metadata;
    this.metadataPrefixes = metadataPrefixes;
    this.datestamp = datestamp;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("OAIRecord with identifier: ").appendValue(id) //
        .appendText(" setSpecs: ").appendValue(setSpecs) //
        .appendText(" metadata: ").appendValue(metadata) //
        .appendText(" metaDataPrefixes: ").appendValue(metadataPrefixes) //
        .appendText(" dateStamp: ").appendValue(datestamp.getTime());
  }

  @Override
  protected void describeMismatchSafely(OAIRecord item, Description mismatchDescription) {
    mismatchDescription.appendText("OAIRecord with identifier: ").appendValue(item.getIdentifier()) //
        .appendText(" setSpecs: ").appendValue(item.getSetSpecs()) //
        .appendText(" metadata: ").appendValue(item.getMetadata()) //
        .appendText(" metaDataPrefixes: ").appendValue(item.getMetadataPrefixes()) //
        .appendText(" dateStamp: ").appendValue(item.getDatestamp().getTime());
  }

  @Override
  protected boolean matchesSafely(OAIRecord item) {
    boolean isEqual = Objects.equal(id, item.getIdentifier());
    isEqual &= Objects.equal(setSpecs, item.getSetSpecs());
    isEqual &= Objects.equal(metadata, item.getMetadata());
    isEqual &= Objects.equal(metadataPrefixes, item.getMetadataPrefixes());
    isEqual &= Objects.equal(datestamp.getTime(), item.getDatestamp().getTime());

    return isEqual;
  }

  public static OAIRecordMatcher likeOAIRecordWith(String identifier, List<String> setSpecs, String metadata, List<String> metadataPrefixes, Date datestamp) {
    return new OAIRecordMatcher(identifier, setSpecs, metadata, metadataPrefixes, datestamp);
  }

}
