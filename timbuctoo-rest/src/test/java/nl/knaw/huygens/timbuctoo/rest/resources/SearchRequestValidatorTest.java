package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.rest.model.TestDomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.junit.Test;

public class SearchRequestValidatorTest {
  private static final String INVALID_VRE_ID = "invalidVRE";
  private static final String VALID_VRE_ID = "vreID";
  private SearchRequestValidator instance;
  private SearchParameters unusedSearchParameters = null;
  private VREManager vreManagerMock;
  private TypeRegistry typeRegistryMock;
  private VRE vreMock;

  @Before
  public void setUp() {
    vreManagerMock = mock(VREManager.class);
    typeRegistryMock = mock(TypeRegistry.class);
    vreMock = mock(VRE.class);
    when(vreManagerMock.getVREById(VALID_VRE_ID)).thenReturn(vreMock);

    instance = new SearchRequestValidator(vreManagerMock, typeRegistryMock);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateNoVREIdSpecified() {
    instance.validate(null, unusedSearchParameters);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateVREUnknown() {
    when(vreManagerMock.getVREById(INVALID_VRE_ID)).thenReturn(null);

    instance.validate(INVALID_VRE_ID, unusedSearchParameters);
  }

  @Test(expected = NullPointerException.class)
  public void testValidateNoSearchParametersSpecified() {
    instance.validate(VALID_VRE_ID, null);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateNoTypeStringSpecified() {
    SearchParameters searchParametersMock = mock(SearchParameters.class);
    when(searchParametersMock.getTypeString()).thenReturn(null);

    try {
      instance.validate(VALID_VRE_ID, searchParametersMock);
    } finally {
      verify(searchParametersMock).getTypeString();
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateTypeUnknown() {
    SearchParameters searchParametersMock = mock(SearchParameters.class);
    String unknownTypeString = "unknownType";

    when(searchParametersMock.getTypeString()).thenReturn(unknownTypeString);
    when(typeRegistryMock.getDomainEntityType(unknownTypeString)).thenReturn(null);

    try {
      instance.validate(VALID_VRE_ID, searchParametersMock);
    } finally {
      verify(searchParametersMock).getTypeString();
      verify(typeRegistryMock).getDomainEntityType(unknownTypeString);
    }

  }

  @Test(expected = TimbuctooException.class)
  public void testValidateTypeIsNotInScope() {
    SearchParameters searchParametersMock = mock(SearchParameters.class);
    String knownTypeString = "testdomainentity";

    when(searchParametersMock.getTypeString()).thenReturn(knownTypeString);
    Class<TestDomainEntity> typeNotInScope = TestDomainEntity.class;
    doReturn(typeNotInScope).when(typeRegistryMock).getDomainEntityType(knownTypeString);
    when(vreMock.inScope(typeNotInScope)).thenReturn(false);

    try {
      instance.validate(VALID_VRE_ID, searchParametersMock);
    } finally {
      verify(searchParametersMock).getTypeString();
      verify(typeRegistryMock).getDomainEntityType(knownTypeString);
      verify(vreMock).inScope(typeNotInScope);
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateNoQueryStringIsSpecified() {
    SearchParameters searchParametersMock = mock(SearchParameters.class);
    String knownTypeString = "testdomainentity";

    when(searchParametersMock.getTypeString()).thenReturn(knownTypeString);
    when(searchParametersMock.getTerm()).thenReturn(null);
    Class<TestDomainEntity> typeNotInScope = TestDomainEntity.class;
    doReturn(typeNotInScope).when(typeRegistryMock).getDomainEntityType(knownTypeString);
    when(vreMock.inScope(typeNotInScope)).thenReturn(true);

    try {
      instance.validate(VALID_VRE_ID, searchParametersMock);
    } finally {
      verify(searchParametersMock).getTypeString();
      verify(typeRegistryMock).getDomainEntityType(knownTypeString);
      verify(vreMock).inScope(typeNotInScope);
      verify(searchParametersMock).getTerm();
    }
  }

  @Test
  public void testValidateValid() {
    SearchParameters searchParametersMock = mock(SearchParameters.class);
    String knownTypeString = "testdomainentity";

    when(searchParametersMock.getTypeString()).thenReturn(knownTypeString);
    when(searchParametersMock.getTerm()).thenReturn("test");
    Class<TestDomainEntity> typeNotInScope = TestDomainEntity.class;
    doReturn(typeNotInScope).when(typeRegistryMock).getDomainEntityType(knownTypeString);
    when(vreMock.inScope(typeNotInScope)).thenReturn(true);

    try {
      instance.validate(VALID_VRE_ID, searchParametersMock);
    } finally {
      verify(searchParametersMock).getTypeString();
      verify(typeRegistryMock).getDomainEntityType(knownTypeString);
      verify(vreMock).inScope(typeNotInScope);
      verify(searchParametersMock).getTerm();
    }
  }
}
