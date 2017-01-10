package nl.knaw.huygens.timbuctoo.security.dataaccess;

import com.codahale.metrics.health.HealthCheck;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.util.Iterator;

public interface AccessFactory {
  LoginAccess getLoginAccess() throws AccessNotPossibleException;

  UserAccess getUserAccess() throws AccessNotPossibleException;

  VreAuthorizationAccess getVreAuthorizationAccess() throws AccessNotPossibleException;

  Iterator<Tuple<String, HealthCheck>> getHealthChecks();
}
