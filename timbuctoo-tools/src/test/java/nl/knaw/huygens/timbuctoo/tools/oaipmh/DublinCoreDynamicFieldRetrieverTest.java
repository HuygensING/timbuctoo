package nl.knaw.huygens.timbuctoo.tools.oaipmh;

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
