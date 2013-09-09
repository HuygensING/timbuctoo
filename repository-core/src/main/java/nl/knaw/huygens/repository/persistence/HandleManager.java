package nl.knaw.huygens.repository.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.PrivateKey;

import net.handle.hdllib.AdminRecord;
import net.handle.hdllib.AuthenticationInfo;
import net.handle.hdllib.ClientSessionTracker;
import net.handle.hdllib.Common;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.Encoder;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.SessionSetupInfo;
import net.handle.hdllib.Util;
import nl.knaw.huygens.repository.config.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to persist and resolve objects on the Handle Server.
 * 
 * @author martijnm
 */
public class HandleManager extends DefaultPersistenceManager {

  private static final Logger LOG = LoggerFactory.getLogger(HandleManager.class);

  /**
   * Returns a new <code>HandleManager</code>.
   */
  public static HandleManager newHandleManager(Configuration config) {
    String url = config.getSetting("public_url");
    String cipherString = config.getSetting("handle.cipher");
    byte[] cipher = cipherString.getBytes();
    byte[] privateKeyBytes = readPrivateKey(config);
    String authority = config.getSetting("handle.naming_authority");
    String prefix = config.getSetting("handle.prefix");
    String adminHandle = getAdminHandle(authority, prefix);

    PublicKeyAuthenticationInfo authenticationInfo = null;
    SessionSetupInfo sessionSetupInfo = null;
    ClientSessionTracker sessionTracker = new ClientSessionTracker();

    try {
      byte[] decryptedKeyBytes = Util.decrypt(privateKeyBytes, cipher);
      PrivateKey privateKey = Util.getPrivateKeyFromBytes(decryptedKeyBytes, 0);
      authenticationInfo = new PublicKeyAuthenticationInfo(Util.encodeString(adminHandle), 300, privateKey);
      sessionSetupInfo = new SessionSetupInfo(authenticationInfo);
    } catch (Exception ex) {
      LOG.error(ex.getMessage());
    }

    sessionTracker.setSessionSetupInfo(sessionSetupInfo);
    HandleResolver resolver = new HandleResolver();
    resolver.setSessionTracker(sessionTracker);

    net.handle.hdllib.Configuration configuration = resolver.getConfiguration();
    //This seems to speed up the communication with the handle server.
    //TODO: Check what this option is doing.
    configuration.setResolutionMethod(net.handle.hdllib.Configuration.RM_WITH_CACHE);

    return new HandleManager(resolver, prefix, authority, url);
  }

  private static byte[] readPrivateKey(Configuration config) {
    FileInputStream inputStream = null;
    try {
      String privateKeyURI = config.getSetting("handle.private_key_file");
      File file = new File(config.pathInUserHome(privateKeyURI));
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

  private final HandleResolver handleResolver;
  private final String namingAuthority;
  private final String prefix;
  private final String baseUrl;

  public HandleManager(HandleResolver resolver, String prefix, String namingAuthority, String baseURL) {
    handleResolver = resolver;
    this.prefix = prefix;
    this.namingAuthority = namingAuthority;
    this.baseUrl = baseURL.endsWith("/") ? baseURL : baseURL + "/";
  }

  @Override
  public String getPersistentURL(String persistenceID) throws PersistenceException {
    String url = null;
    String handleName = createHandleName(persistenceID);

    try {
      HandleValue[] values = handleResolver.resolveHandle(handleName, new String[] { "URL" }, new int[] { 1 });
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
      adminValue = createAdminValue(createAdminHandle(), 200, 100);
      HandleValue[] handleValues = { urlValue, adminValue };
      String handleName = createHandleName(id);

      ClientSessionTracker sessionTracker = handleResolver.getSessionTracker();
      SessionSetupInfo sessionSetupInfo = sessionTracker.getSessionSetupInfo();
      AuthenticationInfo authInfo = sessionSetupInfo.authInfo;
      CreateHandleRequest createRequest = new CreateHandleRequest(Util.encodeString(handleName), handleValues, authInfo);

      handleResolver.processRequest(createRequest);

    } catch (HandleException ex) {
      throw new PersistenceException(ex);
    }

    return id;
  }

  public HandleValue createAdminValue(final String adminHandle, final int keyIndex, int index) throws HandleException {
    AdminRecord adminRecord = new AdminRecord(Util.encodeString(adminHandle), keyIndex, true, true, true, true, true, true, true, true, true, true, true, true);
    return new HandleValue(index, Common.ADMIN_TYPE, Encoder.encodeAdminRecord(adminRecord), HandleValue.TTL_TYPE_RELATIVE, 86400, 0, null, true, true, true, false);
  }

  @Override
  public String persistObject(String collectionId, String objectId) throws PersistenceException {
    String url = createUrl(collectionId, objectId);
    return persistURL(url);
  }

  private String createUrl(String collectionId, String id) {
    return baseUrl + "resources/" + collectionId + "/" + id;
  }

  private String createAdminHandle() {
    return namingAuthority + "/" + prefix;
  }

  private String createHandleName(String id) {
    return prefix + "/" + id;
  }

}
