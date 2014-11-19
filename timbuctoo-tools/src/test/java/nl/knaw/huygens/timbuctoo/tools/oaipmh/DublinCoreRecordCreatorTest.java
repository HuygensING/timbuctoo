package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import static nl.knaw.huygens.timbuctoo.tools.oaipmh.DublinCoreRecordCreator.TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.oaipmh.metadata.DublinCoreRecord;

import org.junit.Before;
import org.junit.Test;

public class DublinCoreRecordCreatorTest {

  private static final String BASE_URL = "http://test.com";
  private static final String URL = "url";
  private DublinCoreRecordCreator instance;
  private DublinCoreDynamicFieldRetriever dcDynamicFieldRetriever;
  private DublinCoreIdentifierCreator dcIdentifierCreator;

  @Before
  public void setUp() {
    dcDynamicFieldRetriever = mock(DublinCoreDynamicFieldRetriever.class);
    dcIdentifierCreator = mock(DublinCoreIdentifierCreator.class);
  }

  @Test
  public void fromDomainSetDefaultFieldsAndLetsDublinCoreDynamicFieldFillerFillTheFieldsOfTheDocument() {
    // setup
    TestDomainEntity entity = new TestDomainEntity();

    final DublinCoreRecord dublinCoreRecordMock = mock(DublinCoreRecord.class);
    instance = new DublinCoreRecordCreator(dcDynamicFieldRetriever, dcIdentifierCreator) {
      @Override
      protected DublinCoreRecord createDublinCoreRecord() {
        return dublinCoreRecordMock;
      }
    };

    // when
    when(dcIdentifierCreator.create(entity, BASE_URL)).thenReturn(URL);

    // action
    DublinCoreRecord actualDCRecord = instance.fromDomainEntity(entity, BASE_URL);

    // verify
    assertThat(actualDCRecord, is(notNullValue(DublinCoreRecord.class)));

    verify(dublinCoreRecordMock).setType(TYPE);
    verify(dublinCoreRecordMock).setIdentifier(URL);
    verifyNoMoreInteractions(dublinCoreRecordMock);
    verify(dcDynamicFieldRetriever).retrieve(dublinCoreRecordMock, entity);
  }
}
