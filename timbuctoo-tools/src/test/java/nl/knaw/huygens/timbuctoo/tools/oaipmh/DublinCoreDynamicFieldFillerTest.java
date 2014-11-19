package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.oaipmh.metadata.DublinCoreRecord;
import nl.knaw.huygens.timbuctoo.oaipmh.DublinCoreMetadataField;

import org.junit.Before;
import org.junit.Test;

public class DublinCoreDynamicFieldFillerTest {

  private static final String STRING_VALUE = "StringValue";
  private DublinCoreDynamicFieldFiller instance;
  private DublinCoreRecord dcRecordMock;
  private DublinCoreValueConverter dcValueConverterMock;

  @Before
  public void setup() {
    dcValueConverterMock = mock(DublinCoreValueConverter.class);
    dcRecordMock = mock(DublinCoreRecord.class);
    instance = new DublinCoreDynamicFieldFiller(dcValueConverterMock);

    when(dcValueConverterMock.convert(anyObject())).thenReturn(STRING_VALUE);
  }

  @Test
  public void addToConvertsTheValueToStringAndAddsItToTheDublinCoreRecord() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.CONTRIBUTOR;
    Object value = new Object();

    // action
    instance.addTo(dcRecordMock, field, value);

    // verify
    verify(dcValueConverterMock).convert(value);
    verify(dcRecordMock).setContributor(anyString());
  }

  @Test
  public void addToDoesNothingIfTheValueIsNull() {
    DublinCoreMetadataField field = DublinCoreMetadataField.CONTRIBUTOR;
    Object value = null;

    // action
    instance.addTo(dcRecordMock, field, value);

    // verify
    verifyZeroInteractions(dcRecordMock, dcValueConverterMock);
  }

  @Test
  public void addToDoesNothingIfTheFieldIsNull() {
    DublinCoreMetadataField field = null;
    Object value = "test";

    // action
    instance.addTo(dcRecordMock, field, value);

    // verify
    verifyZeroInteractions(dcRecordMock, dcValueConverterMock);
  }

  @Test
  public void addToSetsContributerIfTheDublinCoreMetadataFieldIsCONTRIBUTOR() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.CONTRIBUTOR;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setContributor(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsCovarageIfTheDublinCoreMetadataFieldIsCOVERAGE() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.COVERAGE;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setCoverage(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsCreatorIfTheDublinCoreMetadataFieldIsCREATOR() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.CREATOR;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setCreator(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsDateIfTheDublinCoreMetadataFieldIsDATE() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.DATE;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setDate(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsDescriptionIfTheDublinCoreMetadataFieldIsDESCRIPTION() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.DESCRIPTION;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setDescription(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsFormatIfTheDublinCoreMetadataFieldIsFROMAT() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.FORMAT;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setFormat(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsIdentifierIfTheDublinCoreMetadataFieldIsIDENTIFIER() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.IDENTIFIER;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setIdentifier(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsLanguageIfTheDublinCoreMetadataFieldIsLANGUAGE() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.LANGUAGE;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setLanguage(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsPublisherIfTheDublinCoreMetadataFieldIsPUBLISHER() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.PUBLISHER;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setPublisher(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsRelationIfTheDublinCoreMetadataFieldIsRELATION() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.RELATION;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setRelation(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsRightsIfTheDublinCoreMetadataFieldIsRIGHTS() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.RIGHTS;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setRights(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsSourceIfTheDublinCoreMetadataFieldIsSOURCE() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.SOURCE;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setSource(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsSubjectIfTheDublinCoreMetadataFieldIsSUBJECT() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.SUBJECT;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setSubject(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToTitleCovarageIfTheDublinCoreMetadataFieldIsTITLE() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.TITLE;
    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setTitle(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  @Test
  public void addToSetsTypeIfTheDublinCoreMetadataFieldIsTYPE() {
    // setup
    DublinCoreMetadataField field = DublinCoreMetadataField.TYPE;

    // action
    invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(field);

    // verify
    verify(dcRecordMock).setType(anyString());
    verifyNoMoreInteractions(dcRecordMock);
  }

  private void invokeAddToWithDublinCoreMetaDataFieldAndDefaultValue(DublinCoreMetadataField field) {
    Object value = new Object();

    // action
    instance.addTo(dcRecordMock, field, value);
  }

}
