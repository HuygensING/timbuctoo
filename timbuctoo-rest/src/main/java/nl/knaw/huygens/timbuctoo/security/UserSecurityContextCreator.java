package nl.knaw.huygens.timbuctoo.security;

import java.io.IOException;

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.security.client.SecurityContextCreator;
import nl.knaw.huygens.security.client.model.SecurityInformation;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class UserSecurityContextCreator implements SecurityContextCreator {

  private static final Logger LOG = LoggerFactory.getLogger(UserSecurityContextCreator.class);
  private static final String NEW_USER = "NEW_USER";
  private static final String UNVERIFIED_USER_ROLE = "UNVERIFIED_USER";

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
    example.setUserId(securityInformation.getPrincipal().getName());

    User user = findUser(example);

    if (user == null) {
      example.displayName = securityInformation.getDisplayName();
      user = createUser(example);
    }

    UserSecurityContext userSecurityContext = new UserSecurityContext(securityInformation.getPrincipal(), user);

    return userSecurityContext;
  }

  private User createUser(User user) {
    LOG.debug("Create new user: " + user.displayName);
    User returnValue = null;
    try {
      // Set the role to unverified user so the user can still retrieve her / his own user information.
      user.setRoles(Lists.newArrayList(UNVERIFIED_USER_ROLE));

      storageManager.addEntity(User.class, user);

    } catch (IOException e) {
      LOG.error(e.getMessage());
    }
    // This is needed, to be less dependend on the StorageLayer to set the id.
    returnValue = findUser(user);

    // Add this role so the NewUserFilter will detect this user.
    returnValue.getRoles().add(NEW_USER);

    return returnValue;
  }

  private User findUser(final User example) {

    return storageManager.findEntity(User.class, example);
  }

}
