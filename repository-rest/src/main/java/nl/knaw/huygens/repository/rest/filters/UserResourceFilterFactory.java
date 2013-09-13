package nl.knaw.huygens.repository.rest.filters;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.SecurityContext;

import nl.knaw.huygens.repository.model.User;
import nl.knaw.huygens.repository.security.UserSecurityContext;
import nl.knaw.huygens.repository.services.mail.MailSender;
import nl.knaw.huygens.repository.storage.StorageManager;

import org.apache.commons.lang.StringUtils;

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
 *
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
    private static final String NEW_USER = "NEW_USER";
    private static final String ADMIN_ROLE = "ADMIN";
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
        request.getQueryParameters().putSingle("id", user.getId());

        if (securityContext.isUserInRole(NEW_USER)) {
          sendEmail(user);
        }
      }

      return request;
    }

    private void sendEmail(User user) {

      String vreId = user.getVreId();
      User example = new User();
      example.setVreId(vreId);
      example.setRoles(Lists.newArrayList(ADMIN_ROLE));

      User admin = storageManager.searchDocument(User.class, example);

      StringBuilder contentbuilder = new StringBuilder("Beste admin,\n");
      contentbuilder.append(user.displayName);
      contentbuilder.append(" heeft interesse getoond voor je VRE.\n");
      contentbuilder.append("Met vriendelijke groet,\n");
      contentbuilder.append("De datarepository");

      if (admin != null && !StringUtils.isBlank(admin.email)) {
        mailSender.sendMail(admin.email, "Nieuwe gebruiker", contentbuilder.toString());
      }
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
