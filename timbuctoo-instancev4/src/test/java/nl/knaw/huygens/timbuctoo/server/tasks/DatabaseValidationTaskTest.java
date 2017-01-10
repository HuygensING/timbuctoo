package nl.knaw.huygens.timbuctoo.server.tasks;

import com.google.common.collect.ImmutableMultimap;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseValidator;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


public class DatabaseValidationTaskTest {

  @Test
  public void willExecuteTheFirstTime() throws Exception {
    TestExecutor testExecutor = new TestExecutor(5000);

    assertThat(testExecutor.invokeTest(0L, ImmutableMultimap.of()), is(true));
  }

  @Test
  public void willNotExecuteWithinTimeOut() throws Exception {
    TestExecutor testExecutor = new TestExecutor(5000);

    testExecutor.invokeTest(0L, ImmutableMultimap.of());

    assertThat(testExecutor.invokeTest(4999L, ImmutableMultimap.of()), is(false));
  }

  @Test
  public void willExecuteAfterTimeoutPasses() throws Exception {
    TestExecutor testExecutor = new TestExecutor(5000);

    testExecutor.invokeTest(0L, ImmutableMultimap.of());

    assertThat(testExecutor.invokeTest(5001L, ImmutableMultimap.of()), is(true));
  }

  @Test
  public void willExecuteWithinTimeOutIfForced() throws Exception {
    TestExecutor testExecutor = new TestExecutor(5000);

    testExecutor.invokeTest(0L, ImmutableMultimap.of());

    assertThat(testExecutor.invokeTest(1000L, ImmutableMultimap.of("force", "whatever")), is(true));
  }

  private class TestExecutor {
    private Clock testClock;
    private long initialTime;
    private DatabaseValidationTask instance;

    public TestExecutor(int timeOut) {
      DatabaseValidator databaseValidation = mock(DatabaseValidator.class);
      testClock = mock(Clock.class);
      initialTime = 1000L;

      ValidationResult validationResult = new ValidationResult() {
        @Override
        public boolean isValid() {
          return true;
        }

        @Override
        public String getMessage() {
          return "foo";
        }
      };

      given(databaseValidation.check()).willReturn(validationResult);

      instance = new DatabaseValidationTask(databaseValidation, testClock, timeOut);
    }

    public boolean invokeTest(Long at, ImmutableMultimap<String, String> arguments) throws Exception {
      StringWriter out = new StringWriter();
      PrintWriter printWriter = new PrintWriter(out);
      given(testClock.millis()).willReturn(initialTime + at);
      instance.execute(arguments, printWriter);
      out.flush();
      return !out.toString().equals("Still in cooling off period. Use force to override");
    }
  }
}
