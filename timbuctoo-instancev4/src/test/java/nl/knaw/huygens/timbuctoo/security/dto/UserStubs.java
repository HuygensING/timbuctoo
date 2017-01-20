package nl.knaw.huygens.timbuctoo.security.dto;

import javax.annotation.Nullable;

public class UserStubs {

  public static User userWithId(final String userId) {
    return new User() {
      @Nullable
      @Override
      public String getDisplayName() {
        throw new UnsupportedOperationException("Not implemented yet");
      }

      @Nullable
      @Override
      public String getPersistentId() {
        throw new UnsupportedOperationException("Not implemented yet");
      }

      @Override
      public String getId() {
        return userId;
      }
    };
  }
}
