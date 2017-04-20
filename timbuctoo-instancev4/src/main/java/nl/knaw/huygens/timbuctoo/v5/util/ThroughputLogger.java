package nl.knaw.huygens.timbuctoo.v5.util;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

public class ThroughputLogger {
  private Stopwatch watch = Stopwatch.createUnstarted();
  private long count = 0;
  private long lastDumpAt = 0;
  private long lastDump = 0;
  private final int dumpInterval;

  public long getCount() {
    return count;
  }

  public ThroughputLogger(int dumpInterval) {
    watch.start();
    this.dumpInterval = dumpInterval;
  }

  public void tripleProcessed() {
    count++;
    long curSeconds = watch.elapsed(TimeUnit.SECONDS);
    if (watch.elapsed(TimeUnit.SECONDS) > (lastDumpAt + dumpInterval)) {
      //FIXME! replace with metric
      System.out.println(count + ", " + ((count - lastDump) / dumpInterval / 1000) + "k t/s");
      lastDumpAt = curSeconds;
      lastDump = count;
    }
  }
}
