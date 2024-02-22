package nl.knaw.huygens.timbuctoo.util;

import java.util.concurrent.TimeUnit;

public record Timeout(long duration, TimeUnit timeUnit) {
  public long toMilliseconds() {
    return timeUnit.toMillis(duration);
  }
}
