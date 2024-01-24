package nl.knaw.huygens.timbuctoo.dropwizard.endpoints.auth;

import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.util.Either;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataSetCreationException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.security.UserValidator;
import nl.knaw.huygens.timbuctoo.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.security.exceptions.PermissionFetchingException;
import nl.knaw.huygens.timbuctoo.security.exceptions.UserValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Optional;

public class AuthCheck {
  private static final Logger LOG = LoggerFactory.getLogger(AuthCheck.class);
  private final UserValidator userValidator;
  private final PermissionFetcher permissionFetcher;
  private final DataSetRepository dataSetRepository;

  public AuthCheck(UserValidator userValidator, PermissionFetcher permissionFetcher,
                   DataSetRepository dataSetRepository) {
    this.userValidator = userValidator;
    this.permissionFetcher = permissionFetcher;
    this.dataSetRepository = dataSetRepository;
  }

  private static Either<Response, User> getUser(String authHeader, UserValidator userValidator) {
    try {
      return userValidator.getUserFromAccessToken(authHeader)
        .map(Either::<Response, User>right)
        .orElseGet(() -> Either.left(Response.status(Response.Status.UNAUTHORIZED).build()));
    } catch (UserValidationException e) {
      return Either.left(Response.status(Response.Status.UNAUTHORIZED).build());
    }
  }

  private Either<Response, Tuple<User, DataSet>> handleForceCreate(
      String ownerId, String dataSetId, Optional<String> baseUri, boolean forceCreation, User user) {
    if (forceCreation) {
      if (dataSetRepository.userMatchesPrefix(user, ownerId)) {
        try {
          final DataSet dataSet = dataSetRepository.createDataSet(user, dataSetId, baseUri, null);
          return Either.right(Tuple.tuple(user, dataSet));
        } catch (DataStoreCreationException | DataSetCreationException e) {
          LOG.error(e.getMessage());
          return Either.left(Response.serverError().build());
        } catch (IllegalDataSetNameException e) {
          return Either.left(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
        }
      }
    }
    return Either.left(Response.status(Response.Status.FORBIDDEN).build());
  }

  public Either<Response, Tuple<User, DataSet>> getOrCreate(String authHeader, String ownerId, String dataSetId,
                                                            boolean forceCreation) {
    return getOrCreate(authHeader, ownerId, dataSetId, Optional.empty(), forceCreation);
  }

  public Either<Response, Tuple<User, DataSet>> getOrCreate(String authHeader, String ownerId, String dataSetId,
                                                            Optional<String> baseUri, boolean forceCreation) {
    return getUser(authHeader, userValidator)
      .flatMap(user ->
        dataSetRepository.getDataSet(user, ownerId, dataSetId)
          .map(ds -> Either.<Response, Tuple<User, DataSet>>right(Tuple.tuple(user, ds)))
          .orElseGet(() -> handleForceCreate(ownerId, dataSetId, baseUri, forceCreation, user))
      );
  }

  public Either<Response, Tuple<User, DataSet>> allowedToImport(User user, DataSet dataSet) {
    try {
      if (permissionFetcher.hasPermission(user,dataSet.getMetadata(), Permission.IMPORT_DATA)) {
        return Either.right(Tuple.tuple(user, dataSet));
      } else {
        return Either.left(Response.status(Response.Status.FORBIDDEN).build());
      }
    } catch (PermissionFetchingException e) {
      return Either.left(Response.serverError().build());
    }
  }
}
