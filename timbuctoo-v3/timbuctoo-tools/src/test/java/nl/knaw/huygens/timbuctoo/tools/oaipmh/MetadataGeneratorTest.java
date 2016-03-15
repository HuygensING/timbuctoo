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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.oaipmh.metadata.CMDIDublinCoreRecord;
import nl.knaw.huygens.oaipmh.metadata.DublinCoreRecord;

import org.junit.Before;
import org.junit.Test;

public class MetadataGeneratorTest {
  private static final String OAI_URL = "http://www.example.org";
  private static final String BASE_URL = "http://www.example.com";
  private static final String VRE_ID = "e-BNM+";
  private static final String BOGUS_XML = "Bogus XML";
  private static final String IDENTIFIER = "identifier";
  private static final TestDomainEntity TEST_DOMAIN_ENTITY = new TestDomainEntity();
  private DublinCoreRecord dcRecordMock;
  private DublinCoreRecordCreator dcRecordCreatorMock;
  private CMDIDublinCoreRecordCreator cmdiDCRecordCreatorMock;
  private CMDIDublinCoreRecord cmdiDCRecordMock;
  private MetadataGenerator instance;

  @Before
  public void setUp() {
    dcRecordMock = mock(DublinCoreRecord.class);
    dcRecordCreatorMock = mock(DublinCoreRecordCreator.class);
    cmdiDCRecordCreatorMock = mock(CMDIDublinCoreRecordCreator.class);
    cmdiDCRecordMock = mock(CMDIDublinCoreRecord.class);

    instance = new MetadataGenerator(dcRecordCreatorMock, cmdiDCRecordCreatorMock);
  }

  @Test
  public void createsAMetadataStringFromADublinCoreRecordAndACMDIRecord() {

    // when
    when(dcRecordCreatorMock.fromDomainEntity(TEST_DOMAIN_ENTITY, BASE_URL)).thenReturn(dcRecordMock);
    when(dcRecordMock.asXML()).thenReturn(BOGUS_XML);

    when(cmdiDCRecordCreatorMock.create(dcRecordMock, TEST_DOMAIN_ENTITY, IDENTIFIER, VRE_ID, OAI_URL)).thenReturn(cmdiDCRecordMock);
    when(cmdiDCRecordMock.asXML()).thenReturn(BOGUS_XML);

    // action
    String actualMetadata = instance.generate(TEST_DOMAIN_ENTITY, IDENTIFIER, VRE_ID, BASE_URL, OAI_URL);

    // verify
    assertThat(actualMetadata, is(notNullValue(String.class)));
    assertThat(actualMetadata, equalTo("<meta>" + BOGUS_XML + BOGUS_XML + "</meta>"));

    verify(dcRecordMock).asXML();
    verify(cmdiDCRecordMock).asXML();
  }

}
