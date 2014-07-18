package nl.knaw.huygens.timbuctoo.tools.process;

/*
 * #%L
 * Timbuctoo tools
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 * A sequence of tasks.
 */
public class Pipeline implements Task {

 private static final Logger LOG = LoggerFactory.getLogger(Pipeline.class);

  /**
   * Executes the provided tasks.
   */
  public static void execute(Task... tasks) throws Exception {
    Pipeline pipeline = new Pipeline();
    for (Task task : tasks) {
      pipeline.add(task);
    }
    pipeline.call();
  }

  // -------------------------------------------------------------------

  private final List<Task> tasks;

  public Pipeline() {
    tasks = Lists.newArrayList();
  }

  public void add(Task task) {
    Preconditions.checkNotNull(task);
    tasks.add(task);
  }

  @Override
  public String getDescription() {
    return Pipeline.class.getName();
  }

  @Override
  public void call() throws Exception {
    Stopwatch stopWatch = Stopwatch.createStarted();
    for (Task task : tasks) {
      LOG.info(task.getDescription());
      task.call();
    }
    stopWatch.stop();
    LOG.info("Time used: {}", stopWatch);
  }

}
