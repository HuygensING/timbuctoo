package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import javaslang.control.Either;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCrud;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.dto.UserRoles;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class VreAuthIniter {
  private static final Logger LOG = getLogger(VreAuthIniter.class);
  protected final UserValidator userValidator;
  private TransactionEnforcer transactionEnforcer;
  private VreAuthorizationCrud authorizationCreator;

  public VreAuthIniter(UserValidator userValidator, TransactionEnforcer transactionEnforcer,
                       VreAuthorizationCrud authorizationCreator) {
    this.userValidator = userValidator;
    this.transactionEnforcer = transactionEnforcer;
    this.authorizationCreator = authorizationCreator;
  }

  public Either<String, Response> addVreAuthorizations(String authorization, String vreName) {
    Optional<User> user;

    try {
      user = this.userValidator.getUserFromAccessToken(authorization);
    } catch (UserValidationException e) {
      user = Optional.empty();
    }
    if (!user.isPresent()) {
      return Either.right(Response.status(Response.Status.FORBIDDEN).entity("User not known").build());
    }

    if (vreName == null) {
      return Either.right(Response.status(Response.Status.BAD_REQUEST).entity("vreName missing").build());
    }
    String namespacedVre = user.get().getPersistentId() + "_" + stripFunnyCharacters(vreName);

    Vre vre = transactionEnforcer.executeAndReturn(
      timbuctooActions -> TransactionStateAndResult.commitAndReturn(timbuctooActions.getVre(namespacedVre))
    );

    if (vre != null) {
      // not found
      Either.right(Response.status(Response.Status.NOT_FOUND).build());
    }

    try {
      authorizationCreator.createAuthorization(namespacedVre, user.get().getId(), UserRoles.ADMIN_ROLE);
    } catch (AuthorizationCreationException e) {
      LOG.error("Cannot add authorization for user {} and VRE {}", user.get().getId(), namespacedVre);
      LOG.error("Exception thrown", e);
      return Either
        .right(Response.status(Response.Status.FORBIDDEN).entity("Unable to create authorization for user").build());
    }
    return Either.left(namespacedVre);
  }


  private String stripFunnyCharacters(String vre) {
    return vre.replaceFirst("\\.[a-zA-Z]+$", "").replaceAll("[^a-zA-Z-]", "_");
  }


}
