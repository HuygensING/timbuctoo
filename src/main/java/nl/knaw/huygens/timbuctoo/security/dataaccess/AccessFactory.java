package nl.knaw.huygens.timbuctoo.security.dataaccess;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.security.PermissionConfiguration;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.security.exceptions.AccessNotPossibleException;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface AccessFactory {
  UserAccess getUserAccess() throws AccessNotPossibleException;

  VreAuthorizationAccess getVreAuthorizationAccess() throws AccessNotPossibleException;

  Iterator<Tuple<String, Supplier<Optional<String>>>> getHealthChecks();

  PermissionConfiguration getPermissionConfig() throws AccessNotPossibleException;
}
