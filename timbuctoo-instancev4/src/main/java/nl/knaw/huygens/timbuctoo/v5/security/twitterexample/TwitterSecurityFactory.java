package nl.knaw.huygens.timbuctoo.v5.security.twitterexample;

import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AccessNotPossibleException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class TwitterSecurityFactory implements SecurityFactory {

  private final TwitterUserValidator twitterUserValidator;

  public TwitterSecurityFactory() {
    twitterUserValidator = new TwitterUserValidator();
  }

  @Override
  public Iterator<Tuple<String, Supplier<Optional<String>>>> getHealthChecks() {
    return Collections.emptyIterator();
  }

  @Override
  public UserValidator getUserValidator() throws AccessNotPossibleException, NoSuchAlgorithmException {
    return twitterUserValidator;
  }

  @Override
  public PermissionFetcher getPermissionFetcher() throws AccessNotPossibleException, NoSuchAlgorithmException {
    return new PermissionFetcher() {
      @Override
      public Set<Permission> getPermissions(String persistentId, PromotedDataSet dataSet)
        throws PermissionFetchingException {

        HashSet<Permission> result = new HashSet<>();
        if (persistentId != null && persistentId.equals(dataSet.getOwnerId())) {
          result.add(Permission.ADMIN);
          result.add(Permission.WRITE);
        } else {
          result.add(Permission.READ);
        }
        return result;
      }

      @Override
      public Set<Permission> getOldPermissions(String persistentId, String vreId) throws PermissionFetchingException {
        HashSet<Permission> result = new HashSet<>();
        result.add(Permission.READ);
        return result;
      }

      @Override
      public void initializeOwnerAuthorization(String userId, String ownerId,
                                               String dataSetId) throws PermissionFetchingException,
        AuthorizationCreationException {
      }

      @Override
      public void removeAuthorizations(String ownerId, String vreId) throws PermissionFetchingException {
      }
    };
  }
}
