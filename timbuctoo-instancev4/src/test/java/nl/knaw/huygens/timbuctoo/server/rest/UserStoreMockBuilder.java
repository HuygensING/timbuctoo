package nl.knaw.huygens.timbuctoo.server.rest;

import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserStoreMockBuilder {

  private final JsonBasedUserStore userStore;

  private UserStoreMockBuilder() {
    userStore = mock(JsonBasedUserStore.class);
    when(userStore.userFor(anyString())).thenReturn(Optional.empty());
  }

  public JsonBasedUserStore build() {
    return userStore;
  }

  public static UserStoreMockBuilder userStore() {
    return new UserStoreMockBuilder();
  }

  public UserStoreMockBuilder withUserFor(String pid) {
    when(userStore.userFor(pid)).thenReturn(Optional.of(new User()));
    return this;
  }

}
