package nl.knaw.huygens.timbuctoo.rest.filters;

import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.USER_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.security.UserSecurityContext;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * A resource filter that sends an e-mail if a new user has logged in.
 * @author martijnm
 */
public class UserResourceFilterFactory implements ResourceFilterFactory {
  private final StorageManager storageManager;
  private final MailSender mailSender;

  @Inject
  public UserResourceFilterFactory(StorageManager storageManager, MailSender mailSender) {
    this.storageManager = storageManager;
    this.mailSender = mailSender;
  }

  @Override
  public List<ResourceFilter> create(AbstractMethod abstractMethod) {
    return Collections.<ResourceFilter> singletonList(new UserResourceFilter(storageManager, mailSender));
  }

  private static class UserResourceFilter implements ResourceFilter, ContainerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(UserResourceFilter.class);
    private final StorageManager storageManager;
    private final MailSender mailSender;

    public UserResourceFilter(StorageManager storageManager, MailSender mailSender) {
      this.storageManager = storageManager;
      this.mailSender = mailSender;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
      SecurityContext securityContext = request.getSecurityContext();

      if (securityContext instanceof UserSecurityContext) {
        User user = ((UserSecurityContext) securityContext).getUser();
        String userId = user.getId();
        String vreId = request.getHeaderValue(VRE_ID_KEY);

        // Set the user id and the current vre id.
        request.getQueryParameters().putSingle(USER_ID_KEY, userId);
        request.getQueryParameters().putSingle(VRE_ID_KEY, vreId);

        VREAuthorization vreAuthorization = getVreAuthorization(userId, vreId);

        // The user is not know with the VRE, if vreAuthorization is equal to null. 
        if (vreAuthorization == null) {
          try {
            vreAuthorization = createVreAuthorization(userId, vreId);
          } catch (IOException e) {
            LOG.error("Creation of VREAuthorization for user with id {} and vre {} failed", userId, vreId);
          }
          sendEmail(user, vreId);
        }

        user.setVreAuthorization(vreAuthorization);
      }

      return request;
    }

    private VREAuthorization createVreAuthorization(String userId, String vreId) throws IOException {
      VREAuthorization vreAuthorization = new VREAuthorization();
      vreAuthorization.setUserId(userId);
      vreAuthorization.setVreId(vreId);
      vreAuthorization.setRoles(Lists.newArrayList(UNVERIFIED_USER_ROLE));

      vreAuthorization.setId(storageManager.addSystemEntity(VREAuthorization.class, vreAuthorization));

      return vreAuthorization;
    }

    private VREAuthorization getVreAuthorization(String userId, String vreId) {

      VREAuthorization example = new VREAuthorization();
      example.setVreId(vreId);
      example.setUserId(userId);

      return storageManager.findEntity(VREAuthorization.class, example);
    }

    /**
     * sends an email to the admin of the VRE the new user is trying to use. 
     * @param user
     * @param vreId 
     */
    private void sendEmail(User user, String vreId) {
      User admin = getFirstAdminOfVRE(vreId);

      StringBuilder contentbuilder = new StringBuilder("Beste admin,\n");
      contentbuilder.append(user.getDisplayName());
      contentbuilder.append(" heeft interesse getoond voor je VRE.\n");
      contentbuilder.append("Met vriendelijke groet,\n");
      contentbuilder.append("De datarepository");

      if (admin != null && !StringUtils.isBlank(admin.getEmail())) {
        mailSender.sendMail(admin.getEmail(), "Nieuwe gebruiker", contentbuilder.toString());
      }
    }

    private User getFirstAdminOfVRE(String vreId) {
      VREAuthorization example = new VREAuthorization();
      example.setRoles(Lists.newArrayList(ADMIN_ROLE));
      example.setVreId(vreId);

      VREAuthorization authorization = storageManager.findEntity(VREAuthorization.class, example);

      return authorization != null ? storageManager.getEntity(User.class, authorization.getUserId()) : null;
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
      return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
      // ReponseFilter not supported
      return null;
    }

  }
}
