package nl.knaw.huygens.repository.rest.security.apis;

import java.io.IOException;

import nl.knaw.huygens.repository.mail.MailSender;
import nl.knaw.huygens.repository.model.User;
import nl.knaw.huygens.repository.rest.security.UserSecurityContext;
import nl.knaw.huygens.repository.storage.StorageManager;

import org.apache.commons.lang.StringUtils;
import org.surfnet.oaaas.model.VerifyTokenResponse;

import com.google.common.collect.Lists;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

public class SecurityContextCreatorResourceFilter implements ResourceFilter, ContainerRequestFilter {

  private static final String UNVERIFIED_USER_ROLE = "UNVERIFIED_USER";
  private static final String ADMIN_ROLE = "ADMIN";
  private static final String VERIFY_TOKEN_RESPONSE = "VERIFY_TOKEN_RESPONSE";
  private StorageManager storageManager;
  private MailSender mailSender;

  public SecurityContextCreatorResourceFilter(StorageManager storageManager, MailSender mailSender) {
    this.storageManager = storageManager;
    this.mailSender = mailSender;
  }

  @Override
  public ContainerRequest filter(ContainerRequest request) {
    VerifyTokenResponse verifyTokenResponse = (VerifyTokenResponse) request.getProperties().get(VERIFY_TOKEN_RESPONSE);

    if (verifyTokenResponse != null) {
      User example = new User();
      example.setVreId(verifyTokenResponse.getAudience());
      example.setUserId(verifyTokenResponse.getPrincipal().getName());
      example.displayName = verifyTokenResponse.getPrincipal().getAttributes().get("DISPLAY_NAME");

      User user = findUser(example);

      if (user == null) {
        user = createUser(example);
      }

      request.getQueryParameters().putSingle("id", user.getId());

      UserSecurityContext securityContext = new UserSecurityContext(verifyTokenResponse.getPrincipal(), user);

      /*
       * TODO: fill setSecure and setAuthenticationScheme
       * ContainerRequest.isSecure wraps SecurityContext.isSecure 
       * the same is true for 
       * ContainerRequest.getAuthenticationScheme()
       */

      request.setSecurityContext(securityContext);
    }

    return request;
  }

  private User createUser(User user) {
    User returnValue = null;
    try {
      // Set the rol to unverified user so the user can still retrieve her / his own user information.
      user.setRoles(Lists.newArrayList(UNVERIFIED_USER_ROLE));

      storageManager.addDocument(User.class, user);

    } catch (IOException e) {
      e.printStackTrace();
    }
    // This is needed, to be less dependend on the StorageLayer to set the id.
    returnValue = findUser(user);

    sendEmail(returnValue);

    return returnValue;
  }

  private void sendEmail(User user) {

    String vreId = user.getVreId();
    User example = new User();
    example.setVreId(vreId);
    example.setRoles(Lists.newArrayList(ADMIN_ROLE));

    User admin = findUser(example);

    StringBuilder contentbuilder = new StringBuilder("Beste admin,\n");
    contentbuilder.append(user.displayName);
    contentbuilder.append(" heeft interesse getoond voor je VRE.\n");
    contentbuilder.append("Met vriendelijke groet,\n");
    contentbuilder.append("De datarepository");

    if (admin != null && !StringUtils.isBlank(admin.email)) {
      mailSender.sendMail(admin.email, "Nieuwe gebruiker", contentbuilder.toString());
    }
  }

  private User findUser(final User example) {

    return storageManager.searchDocument(User.class, example);
  }

  @Override
  public ContainerRequestFilter getRequestFilter() {
    return this;
  }

  @Override
  public ContainerResponseFilter getResponseFilter() {
    // TODO Auto-generated method stub
    return null;
  }

}