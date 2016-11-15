package nl.knaw.huygens.timbuctoo.database;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.handle.HandleAdder;
import nl.knaw.huygens.timbuctoo.handle.HandleAdderParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AfterSuccessTaskExecutor {
  public static final Logger LOG = LoggerFactory.getLogger(AfterSuccessTaskExecutor.class);
  private final List<Task> tasks;

  public AfterSuccessTaskExecutor() {
    tasks = Lists.newArrayList();
  }


  public void addHandleTask(HandleAdder handleAdder, HandleAdderParameters handleAdderParameters) {
    tasks.add(new Task() {

      @Override
      public void execute() throws Exception {
        handleAdder.add(handleAdderParameters);
      }

      @Override
      public String getDescription() {
        return String.format("Add handle to '%s'", handleAdderParameters);
      }
    });
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

  private interface Task {

    void execute() throws Exception;

    String getDescription();
  }
}
