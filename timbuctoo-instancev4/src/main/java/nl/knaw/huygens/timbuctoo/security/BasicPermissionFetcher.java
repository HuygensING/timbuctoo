package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.UserValidationException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class BasicPermissionFetcher implements PermissionFetcher {
  private final VreAuthorizationCrud vreAuthorizationCrud;
  private final DataSetRepository dataSetRepository;
  private final UserValidator userValidator;

  public BasicPermissionFetcher(VreAuthorizationCrud vreAuthorizationCrud, DataSetRepository dataSetRepository,
                                UserValidator userValidator) {
    this.vreAuthorizationCrud = vreAuthorizationCrud;
    this.dataSetRepository = dataSetRepository;
    this.userValidator = userValidator;
  }

  @Override
  public Set<Permission> getPermissions(String persistentId, String ownerId, String dataSetId)
    throws PermissionFetchingException {
    if (!dataSetRepository.dataSetExists(ownerId, dataSetId)) {
      throw new PermissionFetchingException(String.format("DataSet '%s' does not exist.", dataSetId));
    }

    Set<Permission> permissions = new HashSet<>();
    permissions.add(Permission.READ);


    String vreId = PromotedDataSet.createCombinedId(ownerId, dataSetId);

    try {
      Optional<VreAuthorization> vreAuthorization = vreAuthorizationCrud.getAuthorization(vreId, persistentId);
      if (vreAuthorization.isPresent()) {
        if (vreAuthorization.get().isAllowedToWrite()) {
          permissions.add(Permission.WRITE);
        }
      }
      return permissions;
    } catch (AuthorizationUnavailableException e) {
      return permissions;
    }
  }

  @Override
  public void initializeOwnerAuthorization(String ownerId, String dataSetId) throws AuthorizationCreationException {

    String vreId = PromotedDataSet.createCombinedId(ownerId, dataSetId);

    try {
      vreAuthorizationCrud.createAuthorization(vreId, ownerId, "ADMIN");
    } catch (AuthorizationCreationException e) {
      throw e;
    }

  }

  @Override
  public void removeAuthorizations(String ownerId, String dataSetId) throws PermissionFetchingException {
    String vreId = PromotedDataSet.createCombinedId(ownerId, dataSetId);
    Optional<User> user;

    try {
      user = userValidator.getUserFromId(ownerId);
    } catch (UserValidationException e) {
      throw new PermissionFetchingException(String.format("Could not retrieve User for userId '%s'", ownerId));
    }

    try {
      if (user.isPresent()) {
        vreAuthorizationCrud.deleteVreAuthorizations(vreId, user.get());
      } else {
        throw new PermissionFetchingException(String.format("No User found for userId '%s'", ownerId));
      }
    } catch (AuthorizationException | AuthorizationUnavailableException e) {
      throw new PermissionFetchingException(String.format("Authorization not available for" +
        " userId '%s' .", ownerId));
    }
  }
}
