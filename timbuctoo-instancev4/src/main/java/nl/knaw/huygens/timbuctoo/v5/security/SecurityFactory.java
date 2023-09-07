package nl.knaw.huygens.timbuctoo.v5.security;

import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AccessNotPossibleException;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface SecurityFactory {
  default Iterator<Tuple<String, Supplier<Optional<String>>>> getHealthChecks() {
    return Collections.emptyIterator();
  }

  UserValidator getUserValidator() throws AccessNotPossibleException, NoSuchAlgorithmException;

  PermissionFetcher getPermissionFetcher() throws AccessNotPossibleException, NoSuchAlgorithmException;

  default void register(Consumer<Object> registerToJersey) throws NoSuchAlgorithmException, AccessNotPossibleException {
  }
}
