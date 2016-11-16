package nl.knaw.huygens.timbuctoo.database;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AfterSuccessTaskExecutor {
  public static final Logger LOG = LoggerFactory.getLogger(AfterSuccessTaskExecutor.class);
  private final List<Task> tasks;

  public AfterSuccessTaskExecutor() {
    tasks = Lists.newArrayList();
  }

  public void addTask(Task task) {
    tasks.add(task);
  }

  public void executeTasks() {
    tasks.forEach(task -> {
        try {
          task.execute();
        } catch (Exception e) {
          LOG.error("Could not execute task '{}'", task.getDescription());
        }
      }
    );
  }

  public interface Task {

    void execute() throws Exception;

    String getDescription();
  }
}
