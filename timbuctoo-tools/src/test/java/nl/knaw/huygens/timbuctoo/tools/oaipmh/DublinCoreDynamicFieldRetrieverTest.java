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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import nl.knaw.huygens.oaipmh.metadata.DublinCoreRecord;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.oaipmh.DublinCoreMetadataField;

import org.junit.Before;
import org.junit.Test;

public class DublinCoreDynamicFieldRetrieverTest {
  private static final String STRING_VALUE = "test";
  private DublinCoreDynamicFieldRetriever instance;
  private DublinCoreDynamicFieldFiller dcDynamicFieldFillerMock;
  private DublinCoreRecord dcRecord;

  @Before
  public void setUp() {
    dcDynamicFieldFillerMock = mock(DublinCoreDynamicFieldFiller.class);
    instance = new DublinCoreDynamicFieldRetriever(dcDynamicFieldFillerMock);
    dcRecord = new DublinCoreRecord();
  }

  @Test
  public void retrieveSendsDCFieldOfTheDomainEntityToTheDublinCoreFieldFiller() {
    // setup
    DomainEntityWithMultipleOAIDublinCoreFields entity = new DomainEntityWithMultipleOAIDublinCoreFields();
    entity.setTitle(STRING_VALUE);
    entity.setSubject(STRING_VALUE);

    // action
    instance.retrieve(dcRecord, entity);

    // verify
    verify(dcDynamicFieldFillerMock).addTo(dcRecord, DublinCoreMetadataField.TITLE, STRING_VALUE);
    verify(dcDynamicFieldFillerMock).addTo(dcRecord, DublinCoreMetadataField.SUBJECT, STRING_VALUE);
  }

  @Test
  public void retrieveReactesTheSameWhenTheFieldValueIsNull() {
    // setup
    TestDomainEntity entityWithTitleDCField = new TestDomainEntity();
    entityWithTitleDCField.setTitle(null);

    // action
    instance.retrieve(dcRecord, entityWithTitleDCField);

    // verify
    verify(dcDynamicFieldFillerMock).addTo(dcRecord, DublinCoreMetadataField.TITLE, null);
  }

  @Test
  public void retrieveIgnoresAnnotationsOnMethodsThatReturnVoid() {
    // setup
    DomainEntityWithDublinCoreFieldOnMethodThatReturnsVoid domainEntity = new DomainEntityWithDublinCoreFieldOnMethodThatReturnsVoid();

    verifyNoActionsAfterInvokation(domainEntity);
  }

  @Test
  public void retrieveIgnoresAnnotationsOnMethodsThatAcceptsParameters() {
    // setup
    DomainEntityWithDublinCoreFieldOnMethodThatAcceptsParameters domainEntity = new DomainEntityWithDublinCoreFieldOnMethodThatAcceptsParameters();

    verifyNoActionsAfterInvokation(domainEntity);
  }

  @Test
  public void retrieveIgnoresTheMethodsWithoutOAIDublinCoreAnnotation() {
    // setup
    DomainEntityWithoutOAIDublinCoreFields domainEntity = new DomainEntityWithoutOAIDublinCoreFields();

    verifyNoActionsAfterInvokation(domainEntity);
  }

  private void verifyNoActionsAfterInvokation(DomainEntity domainEntity) {
    // action
    instance.retrieve(dcRecord, domainEntity);

    // verify
    verifyNoMoreInteractions(dcDynamicFieldFillerMock);
  }
}
