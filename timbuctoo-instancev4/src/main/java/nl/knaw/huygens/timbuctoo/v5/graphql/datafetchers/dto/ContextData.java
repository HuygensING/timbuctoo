package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.v5.graphql.security.UserPermissionCheck;
import org.immutables.value.Value;

@Value.Immutable
public interface ContextData {

  UserPermissionCheck getUserPermissionCheck();

  static ContextData contextData(UserPermissionCheck userPermissionCheck) {
    return ImmutableContextData.builder().userPermissionCheck(userPermissionCheck).build();
  }
}
