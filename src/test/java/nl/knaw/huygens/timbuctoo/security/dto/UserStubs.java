package nl.knaw.huygens.timbuctoo.security.dto;

import javax.annotation.Nullable;

public class UserStubs {
  public static User user() {
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

      @Nullable
      @Override
      public String getApiKey() {
        throw new UnsupportedOperationException("Not implemented yet");
      }
    };
  }

  public static User userWithPid(final String pid) {
    return new User() {
      @Nullable
      @Override
      public String getDisplayName() {
        throw new UnsupportedOperationException("Not implemented yet");
      }

      @Nullable
      @Override
      public String getPersistentId() {
        return pid;
      }

      @Nullable
      @Override
      public String getApiKey() {
        throw new UnsupportedOperationException("Not implemented yet");
      }
    };
  }
}
