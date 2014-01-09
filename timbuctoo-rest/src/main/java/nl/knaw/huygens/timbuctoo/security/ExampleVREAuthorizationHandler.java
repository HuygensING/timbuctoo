package nl.knaw.huygens.timbuctoo.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

public class ExampleVREAuthorizationHandler implements VREAuthorizationHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleVREAuthorizationHandler.class);

  private final StorageManager storageManager;

  @Inject
  public ExampleVREAuthorizationHandler(StorageManager storageManager) {
    this.storageManager = storageManager;
  }

  @Override
  public VREAuthorization getVREAuthorization(User user, String vreId) {
    String persistentId = user.getPersistentId();

    VREAuthorization example = new VREAuthorization();
    example.setUserId(user.getId());
    example.setVreId(vreId);

    VREAuthorization vreAuthorization = storageManager.findEntity(VREAuthorization.class, example);

    if (vreAuthorization == null) {
      vreAuthorization = example;
      if ("Admin".equals(persistentId)) {
        vreAuthorization.setRoles(Lists.newArrayList(UserRoles.ADMIN_ROLE));
      } else if ("User".equals(persistentId)) {
        vreAuthorization.setRoles(Lists.newArrayList(UserRoles.USER_ROLE));
      }

      try {
        vreAuthorization.setId(storageManager.addSystemEntity(VREAuthorization.class, vreAuthorization));
      } catch (IOException e) {
        LOG.error("Creating VREAuthorization for user {} and VRE {} thrown exception", user.getCommonName(), vreId, e);
      }

    }

    return vreAuthorization;
  }

}
