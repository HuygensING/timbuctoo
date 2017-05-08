package nl.knaw.huygens.timbuctoo.v5.util;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class ThroughputLogger {
  private final String identifier;
  private Stopwatch watch = Stopwatch.createUnstarted();
  private long count = 0;
  private long lastDumpAt = 0;
  private long lastDump = 0;
  private final int dumpInterval;
  private static final Logger LOG = getLogger(ThroughputLogger.class);

  public long getCount() {
    return count;
  }

  public ThroughputLogger(int dumpInterval, String identifier) {
    this.identifier = identifier;
    watch.start();
    this.dumpInterval = dumpInterval;
  }

  public void tripleProcessed() {
    count++;
    long curSeconds = watch.elapsed(TimeUnit.SECONDS);
    if (watch.elapsed(TimeUnit.SECONDS) > (lastDumpAt + dumpInterval)) {
      //FIXME! replace with metric
      LOG.info(identifier + ": " + count + ", " + ((count - lastDump) / dumpInterval / 1000) + "k t/s");
      lastDumpAt = curSeconds;
      lastDump = count;
    }
  }

  public void started() {
    LOG.info(identifier + ": Started");
  }

  public void finished() {
    LOG.info(identifier + ": Finished");
  }

}
