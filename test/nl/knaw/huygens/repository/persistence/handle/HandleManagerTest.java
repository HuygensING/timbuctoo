package nl.knaw.huygens.repository.persistence.handle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import net.handle.api.HSAdapter;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.Util;
import nl.knaw.huygens.repository.persistence.PersistenceException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Maps;

public class HandleManagerTest {

  private HSAdapterFactoryWrapper hsAdapterFactoryWrapper;
  private HSAdapter handleAdapter;
  private HandleManager handleManager;
  private HandleValue adminValue;
  private Map<String, HandleValue[]> handleMap;
  private String prefix = "11151";
  private String namingAuthority = "0.NA";
  private String baseURL = "http://localhost/repository";

  @Before
  public void setUp() throws HandleException {
    handleAdapter = mock(HSAdapter.class);
    hsAdapterFactoryWrapper = mock(HSAdapterFactoryWrapper.class);
    when(hsAdapterFactoryWrapper.createHSAdapter()).thenReturn(handleAdapter);
    handleManager = new HandleManager(hsAdapterFactoryWrapper, prefix, namingAuthority, baseURL);
    adminValue = mock(HandleValue.class);
    handleMap = Maps.newHashMap();

    when(handleAdapter.createAdminValue(anyString(), anyInt(), anyInt())).thenReturn(adminValue);
  }

  @After
  public void tearDown() {
    handleManager = null;
    handleAdapter = null;
    adminValue = null;
    handleMap = null;
  }

  private String createHandleName(String id) {
    return prefix + "/" + id;
  }

  @Test
  public void testPersistURLSuccess() throws HandleException, PersistenceException {
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws HandleException {
        String key = (String) invocation.getArguments()[0];
        HandleValue[] value = (HandleValue[]) invocation.getArguments()[1];
        handleMap.put(key, value);
        return null;
      }
    }).when(handleAdapter).createHandle(anyString(), any(HandleValue[].class));

    String expectedURL = "www.huygens.knaw.nl";
    String id = handleManager.persistURL(expectedURL);
    String handleName = createHandleName(id);
    String actualURL = null;

    HandleValue[] values = handleMap.get(handleName);

    for (HandleValue value : values) {
      if ("URL".equals(value.getTypeAsString())) {
        actualURL = value.getDataAsString();
      }
    }

    assertEquals(2, values.length);
    assertEquals(expectedURL, actualURL);
  }

  @Test(expected = PersistenceException.class)
  public void testPersistURLExceptionThrown() throws HandleException, PersistenceException {
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws HandleException {
        throw new HandleException(1);
      }
    }).when(handleAdapter).createHandle(anyString(), any(HandleValue[].class));

    String urlToPersist = "www.huygens.knaw.nl";
    handleManager.persistURL(urlToPersist);
  }

  @Test
  public void testPersistObjectSuccess() throws HandleException, PersistenceException {
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws HandleException {
        String key = (String) invocation.getArguments()[0];
        HandleValue[] value = (HandleValue[]) invocation.getArguments()[1];
        handleMap.put(key, value);
        return null;
      }
    }).when(handleAdapter).createHandle(anyString(), any(HandleValue[].class));

    String collectionId = "document";
    String objectId = "test000001";

    String id = handleManager.persistObject(collectionId, objectId);

    String handleName = createHandleName(id);
    String expectURL = baseURL + "/resources/document/" + objectId;
    String actualURL = null;

    HandleValue[] values = handleMap.get(handleName);

    for (HandleValue value : values) {
      if ("URL".equals(value.getTypeAsString())) {
        actualURL = value.getDataAsString();
      }
    }

    assertEquals(2, values.length);
    assertEquals(expectURL, actualURL);
  }

  @Test(expected = PersistenceException.class)
  public void testPersistObjectExceptionThrown() throws HandleException, PersistenceException {
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws HandleException {
        throw new HandleException(1);
      }
    }).when(handleAdapter).createHandle(anyString(), any(HandleValue[].class));

    String collectionId = "document";
    String objectId = "test000001";

    handleManager.persistObject(collectionId, objectId);
  }

  @Test
  public void testGetPersistentURLExistingURL() throws HandleException, PersistenceException {
    String id = "test";
    String expectedURL = "www.huygens.knaw.nl";
    HandleValue handleValue = new HandleValue(1, Util.encodeString("URL"), Util.encodeString(expectedURL));
    handleMap.put(createHandleName(id), new HandleValue[] { handleValue });

    doAnswer(new Answer<HandleValue[]>() {
      @Override
      public HandleValue[] answer(InvocationOnMock invocation) throws HandleException {
        String key = (String) invocation.getArguments()[0];
        return handleMap.get(key);
      }
    }).when(handleAdapter).resolveHandle(anyString(), any(String[].class), any(int[].class));

    String actualURL = handleManager.getPersistentURL(id);

    assertEquals(expectedURL, actualURL);
  }

  @Test
  public void testGetPersistentURLNonExistingURL() throws HandleException, PersistenceException {
    doAnswer(new Answer<HandleValue[]>() {
      @Override
      public HandleValue[] answer(InvocationOnMock invocation) throws HandleException {
        return null;
      }
    }).when(handleAdapter).resolveHandle(anyString(), any(String[].class), any(int[].class));
    String id = "test";

    String actualURL = handleManager.getPersistentURL(id);

    assertNull(actualURL);
  }

  @Test(expected = PersistenceException.class)
  public void testGetPersistentURLExceptionThrown() throws HandleException, PersistenceException {
    doAnswer(new Answer<HandleValue[]>() {
      @Override
      public HandleValue[] answer(InvocationOnMock invocation) throws HandleException {
        throw new HandleException(0);
      }
    }).when(handleAdapter).resolveHandle(anyString(), any(String[].class), any(int[].class));

    handleManager.getPersistentURL("test");
  }

}
