package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth;

import javaslang.control.Either;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;
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

  public static Response checkWriteAccess(DataSet dataSet, Optional<User> user, PermissionFetcher permissionFetcher) {
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String currentUserId = user.get().getPersistentId();
    try {
      if (!permissionFetcher.getPermissions(currentUserId,
        dataSet.getMetadata()).contains(Permission.WRITE)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    } catch (PermissionFetchingException e) {
      LOG.error("Authorization unavailable", e);
      //The dataset should already exist, so this is a weird error
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return null;
  }

  public static Response checkAdminAccess(PermissionFetcher permissionFetcher, UserValidator userValidator,
                                          String authHeader,
                                          PromotedDataSet dataSetMetadata) {

    Optional<User> user;
    try {
      user = userValidator.getUserFromAccessToken(authHeader);
    } catch (UserValidationException e) {
      user = Optional.empty();
    }
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String currentUserId = user.get().getPersistentId();

    try {
      if (!permissionFetcher.getPermissions(currentUserId, dataSetMetadata)
        .contains(Permission.ADMIN)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    } catch (PermissionFetchingException e) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    return Response.status(200).build();

  }

  public static Either<Response, User> getUser(String authHeader, UserValidator userValidator) {
    try {
      return userValidator.getUserFromAccessToken(authHeader)
        .map(Either::<Response, User>right)
        .orElseGet(() -> Either.left(Response.status(Response.Status.UNAUTHORIZED).build()));
    } catch (UserValidationException e) {
      return Either.left(Response.status(Response.Status.UNAUTHORIZED).build());
    }
  }

  public Either<Response, Tuple<User, DataSet>> handleForceCreate(String ownerId, String dataSetId,
                                                                  boolean forceCreation, boolean isPublic, User user) {
    if (forceCreation) {
      if (dataSetRepository.userMatchesPrefix(user, ownerId)) {
        try {
          final DataSet dataSet = dataSetRepository.createDataSet(user, dataSetId, isPublic);
          return Either.right(Tuple.tuple(user, dataSet));
        } catch (DataStoreCreationException e) {
          return Either.left(Response.serverError().build());
        } catch (IllegalDataSetNameException e) {
          return Either.left(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
        }
      }
    }
    return Either.left(Response.status(Response.Status.FORBIDDEN).build());
  }

  public Either<Response, Tuple<User, DataSet>> getOrCreate(String authHeader, String ownerId, String dataSetId,
                                                            boolean forceCreation, boolean isPublic) {
    return getUser(authHeader, userValidator)
      .flatMap(user ->
        dataSetRepository.getDataSet(user.getPersistentId(),ownerId, dataSetId)
          .map(ds -> Either.<Response, Tuple<User, DataSet>>right(Tuple.tuple(user, ds)))
          .orElseGet(() -> handleForceCreate(ownerId, dataSetId, forceCreation, isPublic, user))
      );
  }

  public Either<Response, Tuple<User, DataSet>> hasAdminAccess(User user, DataSet dataSet) {
    try {
      if (permissionFetcher.getPermissions(user.getPersistentId(),
        dataSet.getMetadata()).contains(Permission.ADMIN)) {
        return Either.right(Tuple.tuple(user, dataSet));
      } else {
        return Either.left(Response.status(Response.Status.FORBIDDEN).build());
      }
    } catch (PermissionFetchingException e) {
      return Either.left(Response.serverError().build());
    }
  }
}
