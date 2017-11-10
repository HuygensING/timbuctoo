package nl.knaw.huygens.timbuctoo.v5.security.twitterexample;

import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.SecurityFactory;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AccessNotPossibleException;

import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

public class TwitterSecurityFactory implements SecurityFactory {

  private final SecurityFactory oldStyleSecurityFactory;
  private final TwitterUserValidator twitterUserValidator;

  public TwitterSecurityFactory(SecurityFactory oldStyleSecurityFactory) {
    this.oldStyleSecurityFactory = oldStyleSecurityFactory;
    twitterUserValidator = new TwitterUserValidator();
  }

  @Override
  public Iterator<Tuple<String, Supplier<Optional<String>>>> getHealthChecks() {
    return oldStyleSecurityFactory.getHealthChecks();
  }

  @Override
  public UserValidator getUserValidator() throws AccessNotPossibleException, NoSuchAlgorithmException {
    return twitterUserValidator;
  }

  @Override
  public PermissionFetcher getPermissionFetcher() throws AccessNotPossibleException, NoSuchAlgorithmException {
    return oldStyleSecurityFactory.getPermissionFetcher();
  }
}
