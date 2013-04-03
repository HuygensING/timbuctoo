package nl.knaw.huygens.repository.persistence.handle;

import net.handle.api.HSAdapter;
import net.handle.api.HSAdapterFactory;
import net.handle.hdllib.HandleException;

/**
 * This class is needed to create an instance of the HSAdapter on the moment it
 * is needed. It basically stores the connection-settings and provides them to
 * the HSAdaptorFactory when needed.
 * 
 * @author martijnm
 */
public class HSAdapterFactoryWrapper {

  private final String adminHandle;
  private final int keyIndex;
  private final byte[] privateKey;
  private final byte[] cipher;

  public HSAdapterFactoryWrapper(String adminHandle, int keyIndex, byte[] privateKey, byte[] cipher) {
    this.adminHandle = adminHandle;
    this.keyIndex = keyIndex;
    this.privateKey = privateKey;
    this.cipher = cipher;
  }

  public HSAdapter createHSAdapter() throws HandleException {
    return HSAdapterFactory.newInstance(adminHandle, keyIndex, privateKey, cipher);
  }

}
