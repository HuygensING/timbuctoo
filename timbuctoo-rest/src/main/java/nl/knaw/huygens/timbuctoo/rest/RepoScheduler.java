package nl.knaw.huygens.timbuctoo.rest;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * Schedules tasks to be executed in the web application.
 * Currently the cleanup of stored search results.
 * 
 * TODO Investigate applicability of ActiveMQ instead of Quartz
 */
public class RepoScheduler {

  private static final Logger LOG = LoggerFactory.getLogger(RepoScheduler.class);

  public static Injector injector;

  private Scheduler scheduler;
  private long interval;
  private long ttl;

  public RepoScheduler(Injector injector, long interval, long ttl) {
    RepoScheduler.injector = injector;
    this.interval = interval;
    this.ttl = ttl;
  }

  public void start() {
    LOG.info("Starting");
    try {
      System.setProperty("org.quartz.threadPool.threadCount", "3");
      scheduler = StdSchedulerFactory.getDefaultScheduler();
      scheduler.start();
      JobDetail job = newJob(SearchResultCleanupJob.class).withIdentity("cleanup-job", "storage").build();
      Trigger trigger = newTrigger().withIdentity("cleanup-trigger", "storage").usingJobData("ttl", ttl).withSchedule(simpleSchedule().withIntervalInMilliseconds(interval).repeatForever()).build();
      scheduler.scheduleJob(job, trigger);
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    LOG.info("Stopping");
    if (scheduler != null) {
      try {
        scheduler.shutdown();
      } catch (SchedulerException e) {
        LOG.error("Error while stopping scheduler: {}", e.getMessage());
      }
    }
    LOG.info("Stopped");
  }

  // -------------------------------------------------------------------

  public static class SearchResultCleanupJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(SearchResultCleanupJob.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
      long ttl = context.getMergedJobDataMap().getLongValue("ttl");
      Date date = new Date(System.currentTimeMillis() - ttl);
      StorageManager manager = RepoScheduler.injector.getInstance(StorageManager.class);
      int n = manager.deleteSearchResultsBefore(date);
      LOG.info("Removed {} search results", n);
    }
  }

}
