package nl.knaw.huygens.timbuctoo.server.rest;

import java.util.concurrent.TimeUnit;

public class Timeout {
  public final long duration;
  public final TimeUnit timeUnit;

  public Timeout(long duration, TimeUnit timeUnit) {
    this.duration = duration;
    this.timeUnit = timeUnit;
  }

  public long toMilliseconds() {
    return timeUnit.toMillis(duration);
  }
}
