package nl.knaw.huygens.timbuctoo.rest;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import nl.knaw.huygens.timbuctoo.storage.StorageException;
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
  private final long interval;
  private final long ttl;

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

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
      try {
        long ttl = context.getMergedJobDataMap().getLongValue("ttl");
        Date date = new Date(System.currentTimeMillis() - ttl);
        StorageManager manager = RepoScheduler.injector.getInstance(StorageManager.class);
        int n = manager.deleteSearchResultsBefore(date);
        LOG.info("Removed {} search results", n);
      } catch (StorageException e) {
        LOG.error("Failed to remove search results");
        throw new JobExecutionException("Failed to remove search results", e);
      }
    }
  }

}
