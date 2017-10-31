package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.auth;

import javaslang.control.Either;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUsers;
import nl.knaw.huygens.timbuctoo.security.dto.Authorization;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints.ErrorResponseHelper;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class AuthCheck {
  private static final Logger LOG = LoggerFactory.getLogger(AuthCheck.class);
  private final LoggedInUsers loggedInUsers;
  private final PermissionFetcher permissionFetcher;
  private final ErrorResponseHelper errorResponseHelper;
  private final DataSetRepository dataSetRepository;

  public AuthCheck(LoggedInUsers loggedInUsers, PermissionFetcher permissionFetcher,
                   ErrorResponseHelper errorResponseHelper,
                   DataSetRepository dataSetRepository) {
    this.loggedInUsers = loggedInUsers;
    this.permissionFetcher = permissionFetcher;
    this.errorResponseHelper = errorResponseHelper;
    this.dataSetRepository = dataSetRepository;
  }

  public static Response checkWriteAccess(DataSet dataSet, Optional<User> user, PermissionFetcher permissionFetcher) {
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String currentUserId = user.get().getPersistentId();
    try {
      if (!permissionFetcher.getPermissions(currentUserId, dataSet.getMetadata().getOwnerId(),
        dataSet.getMetadata().getDataSetId()).contains(Permission.WRITE)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    } catch (PermissionFetchingException e) {
      LOG.error("Authorization unavailable", e);
      //The dataset should already exist, so this is a weird error
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return null;
  }

  public static Response checkWriteAccess(String ownerId, String dataSetId,
                                          BiFunction<String, String, Optional<PromotedDataSet>> dataSetGetter,
                                          PermissionFetcher permissionFetcher,
                                          LoggedInUsers loggedInUsers, String authHeader) {

    if (ownerId == null || ownerId.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    if (dataSetId == null || dataSetId.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Optional<User> user = loggedInUsers.userFor(authHeader);

    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String currentUserId = user.get().getPersistentId();
    final Optional<PromotedDataSet> dataSet = dataSetGetter.apply(ownerId, dataSetId);
    if (dataSet.isPresent()) {
      try {
        if (!permissionFetcher.getPermissions(currentUserId, dataSet.get().getOwnerId(), dataSet.get().getDataSetId())
          .contains(Permission.WRITE)) {
          return Response.status(Response.Status.FORBIDDEN).build();
        }
      } catch (PermissionFetchingException e) {
        // The dataSet does not yet exist. It might be created by getOrCreate below. So check if the user is
        // accessing a dataSet under his or her own namespace
        if (!ownerId.equals(currentUserId)) {
          return Response.status(Response.Status.FORBIDDEN).build();
        }
      }
    } else if (!Objects.equals(currentUserId, ownerId)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    return null;
  }

  private static Authorization getAuthorization(Authorizer authorizer, PromotedDataSet dataSet,
                                                String currentUserId) throws AuthorizationUnavailableException {
    return authorizer.authorizationFor(dataSet.getCombinedId(), currentUserId);
  }

  public static Response checkAdminAccess(PermissionFetcher permissionFetcher, LoggedInUsers loggedInUsers,
                                          String authHeader,
                                          PromotedDataSet dataSet) {

    Optional<User> user = loggedInUsers.userFor(authHeader);
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String currentUserId = user.get().getPersistentId();

    try {
      if (!permissionFetcher.getPermissions(currentUserId, dataSet.getOwnerId(), dataSet.getDataSetId())
        .contains(Permission.ADMIN)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    } catch (PermissionFetchingException e) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    return Response.status(200).build();

  }

  public static Either<Response, User> getUser(String authHeader, LoggedInUsers loggedInUsers) {
    return loggedInUsers.userFor(authHeader)
      .map(Either::<Response, User>right)
      .orElseGet(() -> Either.left(Response.status(Response.Status.UNAUTHORIZED).build()));
  }

  public Either<Response, Tuple<User, DataSet>> handleForceCreate(String ownerId, String dataSetId,
                                                                  boolean forceCreation, User user) {
    if (forceCreation) {
      if (dataSetRepository.userMatchesPrefix(user, ownerId)) {
        try {
          final DataSet dataSet = dataSetRepository.createDataSet(user, dataSetId);
          return Either.right(Tuple.tuple(user, dataSet));
        } catch (DataStoreCreationException e) {
          return Either.left(Response.serverError().build());
        }
      }
    }
    return Either.left(Response.status(Response.Status.FORBIDDEN).build());
  }

  public Either<Response, Tuple<User, DataSet>> getOrCreate(String authHeader, String ownerId, String dataSetId,
                                                            boolean forceCreation) {
    return getUser(authHeader, loggedInUsers)
      .flatMap(user ->
        dataSetRepository.getDataSet(ownerId, dataSetId)
          .map(ds -> Either.<Response, Tuple<User, DataSet>>right(Tuple.tuple(user, ds)))
          .orElseGet(() -> handleForceCreate(ownerId, dataSetId, forceCreation, user))
      );
  }

  public Either<Response, Tuple<User, DataSet>> hasAdminAccess(User user, DataSet dataSet) {
    try {
      if (permissionFetcher.getPermissions(user.getPersistentId(), dataSet.getMetadata().getOwnerId(),
        dataSet.getMetadata().getDataSetId()).contains(Permission.ADMIN)) {
        return Either.right(Tuple.tuple(user, dataSet));
      } else {
        return Either.left(Response.status(Response.Status.FORBIDDEN).build());
      }
    } catch (PermissionFetchingException e) {
      return Either.left(Response.serverError().build());
    }
  }
}
