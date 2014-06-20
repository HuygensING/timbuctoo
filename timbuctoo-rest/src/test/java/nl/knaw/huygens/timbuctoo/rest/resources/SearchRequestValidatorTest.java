package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.solr.SearchParametersV1;
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
  private SearchParametersV1 unusedSearchParametersV1 = null;
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
    instance.validate(null, unusedSearchParametersV1);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateVREUnknown() {
    when(vreManagerMock.getVREById(INVALID_VRE_ID)).thenReturn(null);

    instance.validate(INVALID_VRE_ID, unusedSearchParametersV1);
  }

  @Test(expected = NullPointerException.class)
  public void testValidateNoSearchParametersV1Specified() {
    instance.validate(VALID_VRE_ID, null);
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateNoTypeStringSpecified() {
    SearchParametersV1 SearchParametersV1Mock = mock(SearchParametersV1.class);
    when(SearchParametersV1Mock.getTypeString()).thenReturn(null);

    try {
      instance.validate(VALID_VRE_ID, SearchParametersV1Mock);
    } finally {
      verify(SearchParametersV1Mock).getTypeString();
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateTypeUnknown() {
    SearchParametersV1 SearchParametersV1Mock = mock(SearchParametersV1.class);
    String unknownTypeString = "unknownType";

    when(SearchParametersV1Mock.getTypeString()).thenReturn(unknownTypeString);
    when(typeRegistryMock.getDomainEntityType(unknownTypeString)).thenReturn(null);

    try {
      instance.validate(VALID_VRE_ID, SearchParametersV1Mock);
    } finally {
      verify(SearchParametersV1Mock).getTypeString();
      verify(typeRegistryMock).getDomainEntityType(unknownTypeString);
    }

  }

  @Test(expected = TimbuctooException.class)
  public void testValidateTypeIsNotInScope() {
    SearchParametersV1 SearchParametersV1Mock = mock(SearchParametersV1.class);
    String knownTypeString = "testdomainentity";

    when(SearchParametersV1Mock.getTypeString()).thenReturn(knownTypeString);
    Class<TestDomainEntity> typeNotInScope = TestDomainEntity.class;
    doReturn(typeNotInScope).when(typeRegistryMock).getDomainEntityType(knownTypeString);
    when(vreMock.inScope(typeNotInScope)).thenReturn(false);

    try {
      instance.validate(VALID_VRE_ID, SearchParametersV1Mock);
    } finally {
      verify(SearchParametersV1Mock).getTypeString();
      verify(typeRegistryMock).getDomainEntityType(knownTypeString);
      verify(vreMock).inScope(typeNotInScope);
    }
  }

  @Test(expected = TimbuctooException.class)
  public void testValidateNoQueryStringIsSpecified() {
    SearchParametersV1 SearchParametersV1Mock = mock(SearchParametersV1.class);
    String knownTypeString = "testdomainentity";

    when(SearchParametersV1Mock.getTypeString()).thenReturn(knownTypeString);
    when(SearchParametersV1Mock.getTerm()).thenReturn(null);
    Class<TestDomainEntity> typeNotInScope = TestDomainEntity.class;
    doReturn(typeNotInScope).when(typeRegistryMock).getDomainEntityType(knownTypeString);
    when(vreMock.inScope(typeNotInScope)).thenReturn(true);

    try {
      instance.validate(VALID_VRE_ID, SearchParametersV1Mock);
    } finally {
      verify(SearchParametersV1Mock).getTypeString();
      verify(typeRegistryMock).getDomainEntityType(knownTypeString);
      verify(vreMock).inScope(typeNotInScope);
      verify(SearchParametersV1Mock).getTerm();
    }
  }

  @Test
  public void testValidateValid() {
    SearchParametersV1 SearchParametersV1Mock = mock(SearchParametersV1.class);
    String knownTypeString = "testdomainentity";

    when(SearchParametersV1Mock.getTypeString()).thenReturn(knownTypeString);
    when(SearchParametersV1Mock.getTerm()).thenReturn("test");
    Class<TestDomainEntity> typeNotInScope = TestDomainEntity.class;
    doReturn(typeNotInScope).when(typeRegistryMock).getDomainEntityType(knownTypeString);
    when(vreMock.inScope(typeNotInScope)).thenReturn(true);

    try {
      instance.validate(VALID_VRE_ID, SearchParametersV1Mock);
    } finally {
      verify(SearchParametersV1Mock).getTypeString();
      verify(typeRegistryMock).getDomainEntityType(knownTypeString);
      verify(vreMock).inScope(typeNotInScope);
      verify(SearchParametersV1Mock).getTerm();
    }
  }
}
