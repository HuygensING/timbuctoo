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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public class AuthCheck {
  private final LoggedInUsers loggedInUsers;
  private final Authorizer authorizer;
  private final ErrorResponseHelper errorResponseHelper;
  private final DataSetRepository dataSetRepository;

  public AuthCheck(LoggedInUsers loggedInUsers, Authorizer authorizer, ErrorResponseHelper errorResponseHelper,
                   DataSetRepository dataSetRepository) {
    this.loggedInUsers = loggedInUsers;
    this.authorizer = authorizer;
    this.errorResponseHelper = errorResponseHelper;
    this.dataSetRepository = dataSetRepository;
  }

  private static final Logger LOG = LoggerFactory.getLogger(AuthCheck.class);

  public static Response checkWriteAccess(DataSet dataSet, Optional<User> user, Authorizer authorizer) {
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String currentUserId = user.get().getPersistentId();
    try {
      final Authorization authorization = getAuthorization(
        authorizer,
        dataSet.getMetadata(),
        currentUserId
      );
      if (!authorization.isAllowedToWrite()) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    } catch (AuthorizationUnavailableException e) {
      LOG.error("Authorization unavailable", e);
      //The dataset should already exist, so this is a weird error
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return null;
  }

  public static Response checkWriteAccess(String ownerId, String dataSetId,
                                          BiFunction<String, String, Optional<PromotedDataSet>> dataSetGetter,
                                          Authorizer authorizer, LoggedInUsers loggedInUsers, String authHeader) {

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
        if (!getAuthorization(authorizer, dataSet.get(), currentUserId).isAllowedToWrite()) {
          return Response.status(Response.Status.FORBIDDEN).build();
        }
      } catch (AuthorizationUnavailableException e) {
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

  public static Response checkAdminAccess(Authorizer authorizer, LoggedInUsers loggedInUsers, String authHeader,
                                          PromotedDataSet dataSet) {

    Optional<User> user = loggedInUsers.userFor(authHeader);
    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String currentUserId = user.get().getPersistentId();

    try {
      if (!getAuthorization(authorizer, dataSet, currentUserId).hasAdminAccess()) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    } catch (AuthorizationUnavailableException e) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    return Response.status(200).build();

  }

  public Either<Response, Tuple<User, DataSet>> handleForceCreate(String ownerId, String dataSetId,
                                                                  boolean forceCreation, User user) {
    if (forceCreation) {
      final String persistentId = user.getPersistentId();
      if (persistentId != null && persistentId.equals(ownerId)) {
        try {
          final DataSet dataSet = dataSetRepository.createDataSet(user.getPersistentId(), dataSetId);
          return Either.right(Tuple.tuple(user, dataSet));
        } catch (DataStoreCreationException e) {
          return Either.left(Response.serverError().build());
        }
      }
    }
    return Either.left(Response.status(Response.Status.FORBIDDEN).build());
  }

  public static Either<Response, User> getUser(String authHeader, LoggedInUsers loggedInUsers) {
    return loggedInUsers.userFor(authHeader)
      .map(Either::<Response, User>right)
      .orElseGet(() -> Either.left(Response.status(Response.Status.UNAUTHORIZED).build()));
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
      final Authorization authorization = authorizer.authorizationFor(
        dataSet.getMetadata().getCombinedId(),
        user.getPersistentId()
      );
      if (authorization.hasAdminAccess()) {
        return Either.right(Tuple.tuple(user, dataSet));
      } else {
        return Either.left(Response.status(Response.Status.FORBIDDEN).build());
      }
    } catch (AuthorizationUnavailableException e) {
      return Either.left(Response.serverError().build());
    }
  }
}
