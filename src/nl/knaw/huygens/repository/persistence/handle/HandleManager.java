package nl.knaw.huygens.repository.persistence.handle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import net.handle.api.HSAdapter;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.Util;
import nl.knaw.huygens.repository.persistence.PersistenceException;
import nl.knaw.huygens.repository.persistence.PersistenceManager;
import nl.knaw.huygens.repository.util.Configuration;
import nl.knaw.huygens.repository.util.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to persist and resolve objects on the Handle Server.
 * 
 * @author martijnm
 */
public class HandleManager implements PersistenceManager {

  private static final Logger LOG = LoggerFactory.getLogger(HandleManager.class);

  /**
   * Returns a new <code>HandleManager</code>.
   */
  public static HandleManager newHandleManager(Configuration config) {
    String url = config.getSetting("public_url");
    String cipherString = config.getSetting("handle.cipher");
    byte[] cipher = cipherString.getBytes();
    String privateKeyURI = config.getSetting("handle.private_key_file");
    byte[] privateKey = readPrivateKey(privateKeyURI);
    String authority = config.getSetting("handle.naming_authority");
    String prefix = config.getSetting("handle.prefix");
    String adminHandle = getAdminHandle(authority, prefix);

    HSAdapterFactoryWrapper wrapper = new HSAdapterFactoryWrapper(adminHandle, 300, privateKey, cipher);
    return new HandleManager(wrapper, prefix, authority, url);
  }

  private static byte[] readPrivateKey(String privateKeyURI) {
    FileInputStream inputStream = null;
    try {
      File file = new File(Paths.pathInUserHome(privateKeyURI));
      LOG.info("Location of private key: {}", file.getAbsolutePath());
      inputStream = new FileInputStream(file);
      byte[] privateKey = new byte[(int) file.length()];
      inputStream.read(privateKey);
      inputStream.close();
      return privateKey;
    } catch (IOException e) {
      LOG.error(e.getMessage());
      return null;
    }
  }

  private static String getAdminHandle(String namingAuthority, String prefix) {
    return namingAuthority + "/" + prefix;
  }

  // -------------------------------------------------------------------

  private final HSAdapterFactoryWrapper hsAdapterFactoryWrapper;
  private final String namingAuthority;
  private final String prefix;
  private final String baseUrl;

  public HandleManager(HSAdapterFactoryWrapper wrapper, String prefix, String namingAuthority, String baseURL) {
    hsAdapterFactoryWrapper = wrapper;
    this.prefix = prefix;
    this.namingAuthority = namingAuthority;
    this.baseUrl = baseURL.endsWith("/") ? baseURL : baseURL + "/";
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
  public String persistURL(String url) throws PersistenceException {
    String id = createId();

    HandleValue urlValue = new HandleValue(1, Util.encodeString("URL"), Util.encodeString(url));
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
  public String persistObject(String collectionId, String objectId) throws PersistenceException {
    String url = createUrl(collectionId, objectId);
    return persistURL(url);
  }

  private String createUrl(String collectionId, String id) {
    return baseUrl + "resources/" + collectionId + "/" + id;
  }

  private String createId() {
    return UUID.randomUUID().toString();
  }

  private String createAdminHandle() {
    return namingAuthority + "/" + prefix;
  }

  private String createHandleName(String id) {
    return prefix + "/" + id;
  }

}
