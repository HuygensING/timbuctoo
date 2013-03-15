package nl.knaw.huygens.repository.persistence.handle;

import java.util.UUID;

import net.handle.api.HSAdapter;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.Util;

import nl.knaw.huygens.repository.persistence.PersistenceException;
import nl.knaw.huygens.repository.persistence.PersistenceManager;

/**
 * A class to persist and resolve objects on the HandleServer.
 * 
 * @author martijnm
 * 
 */
public class HandleManager implements PersistenceManager {
  private String namingAuthority;
  private String prefix;
  private String baseUrl;

  private HSAdapterFactoryWrapper hsAdapterFactoryWrapper;

  public HandleManager(HSAdapterFactoryWrapper hsAdapterFactoryWrapper, String prefix, String namingAuthority, String baseURL) {
    this.hsAdapterFactoryWrapper = hsAdapterFactoryWrapper;
    this.prefix = prefix;
    this.namingAuthority = namingAuthority;
    this.baseUrl = baseURL;
  }

  @Override
  public String getPersistentURL(String persistenceID) throws PersistenceException {
    String url = null;
    String handleName = createHandleName(persistenceID);

    try {
      HSAdapter hsAdapter = hsAdapterFactoryWrapper.createHSAdapter();
      HandleValue[] values = hsAdapter.resolveHandle(handleName, new String[] { "URL" }, new int[] { 1 });
      if (values != null) {
        url = values[0].getDataAsString();
      }
    } catch (HandleException ex) {
      throw new PersistenceException(ex);
    }

    return url;
  }

  @Override
  public String persistURL(String urlToPersist) throws PersistenceException {
    String id = createID();

    HandleValue urlValue = new HandleValue(1, Util.encodeString("URL"), Util.encodeString(urlToPersist));
    urlValue.setAdminCanRead(true);
    urlValue.setAdminCanWrite(true);
    urlValue.setAnyoneCanRead(true);
    urlValue.setAnyoneCanWrite(false);

    HandleValue adminValue = null;
    try {
      HSAdapter hsAdapter = hsAdapterFactoryWrapper.createHSAdapter();
      adminValue = hsAdapter.createAdminValue(createAdminHandle(), 200, 100);
      HandleValue[] handleValues = { urlValue, adminValue };
      hsAdapter.createHandle(createHandleName(id), handleValues);
    } catch (HandleException ex) {
      throw new PersistenceException(ex);
    }

    return id;
  }

  @Override
  public String persistObject(String objectId, String collectionId) throws PersistenceException {
    String url = createUrl(objectId, collectionId);
    return persistURL(url);
  }

  private String createUrl(String id, String collectionId) {

    StringBuilder url = new StringBuilder(baseUrl);
    if (!baseUrl.endsWith("/")) {
      url.append("/");
    }
    url.append("resources/");
    url.append(collectionId);
    url.append("/");
    url.append(id);

    return url.toString();
  }

  private String createID() {
    return UUID.randomUUID().toString();
  }

  private String createAdminHandle() {
    StringBuilder sb = new StringBuilder();
    sb.append(namingAuthority);
    sb.append("/");
    sb.append(prefix);
    return sb.toString();
  }

  private String createHandleName(String id) {
    return prefix + "/" + id;
  }

}
