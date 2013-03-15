package nl.knaw.huygens.repository.persistence.handle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.inject.Inject;

import nl.knaw.huygens.repository.persistence.PersistenceManagerFactory;
import nl.knaw.huygens.repository.util.Configuration;

public class HandleManagerFactory implements PersistenceManagerFactory {
  private Configuration configuration;

  @Inject
  public HandleManagerFactory(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public HandleManager createPersistenceManager() {

    String url = configuration.getSetting("public_url");
    String cipherString = configuration.getSetting("handle.cipher");
    byte[] cipher = cipherString.getBytes();
    String privateKeyURI = configuration.getSetting("handle.private_key_file");
    byte[] privateKey = readPrivateKey(privateKeyURI);
    String prefix = configuration.getSetting("handle.prefix");
    String namingAuthority = configuration.getSetting("handle.naming_authority");
    String adminHandle = createAdminHandle(namingAuthority, prefix);

    HSAdapterFactoryWrapper hsAdapterFactoryWrapper = new HSAdapterFactoryWrapper(adminHandle, 300, privateKey, cipher);

    HandleManager manager = new HandleManager(hsAdapterFactoryWrapper, prefix, namingAuthority, url);

    return manager;
  }

  private byte[] readPrivateKey(String privateKeyURI) {
    byte[] privateKey = null;
    FileInputStream inputStream = null;
    File privateKeyFile = new File(privateKeyURI);
    int fileLength = (int) privateKeyFile.length();

    try {
      inputStream = new FileInputStream(privateKeyFile);

      privateKey = new byte[fileLength];

      inputStream.read(privateKey);

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    return privateKey;
  }

  private String createAdminHandle(String namingAuthority, String prefix) {
    StringBuilder sb = new StringBuilder(namingAuthority);
    sb.append("/");
    sb.append(prefix);

    return sb.toString();
  }

}
