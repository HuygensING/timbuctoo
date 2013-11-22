package nl.knaw.huygens.timbuctoo.security;

import java.io.IOException;

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.security.client.SecurityContextCreator;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class UserSecurityContextCreator implements SecurityContextCreator {

  private static final Logger LOG = LoggerFactory.getLogger(UserSecurityContextCreator.class);

  private final StorageManager storageManager;

  @Inject
  public UserSecurityContextCreator(StorageManager storageManager) {
    this.storageManager = storageManager;
  }

  @Override
  public SecurityContext createSecurityContext(SecurityInformation securityInformation) {
    if (securityInformation == null) {
      return null;
    }

    User example = new User();
    example.setPersistentId(securityInformation.getPersistentID());

    User user = findUser(example);

    if (user == null) {
      example.setDisplayName(securityInformation.getDisplayName());
      user = createUser(example, securityInformation);
    }

    UserSecurityContext userSecurityContext = new UserSecurityContext(securityInformation.getPrincipal(), user);

    return userSecurityContext;
  }

  private User createUser(User user, SecurityInformation securityInformation) {
    LOG.debug("Create new user: " + user.getDisplayName());
    User returnValue = null;

    user.setCommonName(securityInformation.getCommonName());
    user.setDisplayName(securityInformation.getDisplayName());
    user.setEmail(securityInformation.getEmailAddress());
    user.setFirstName(securityInformation.getGivenName());
    user.setLastName(securityInformation.getSurname());
    user.setPersistentId(securityInformation.getPersistentID());
    user.setOrganisation(securityInformation.getOrganization());

    try {

      storageManager.addEntity(User.class, user);

    } catch (IOException e) {
      LOG.error(e.getMessage());
    }
    // This is needed, to be less dependend on the StorageLayer to set the id.
    returnValue = findUser(user);

    return returnValue;
  }

  private User findUser(final User example) {

    return storageManager.findEntity(User.class, example);
  }

}
