package nl.knaw.huygens.timbuctoo.remote.rs.view;

/**
 *
 */
public class ErrorView {

  private String exception;

  public ErrorView(Throwable error) {
    init(error, new Interpreter() {});
  }

  public ErrorView(Throwable error, Interpreter interpreter) {
    init(error, interpreter);
  }

  private void init(Throwable error, Interpreter interpreter) {
    exception = interpreter.getErrorInterpreter().apply(error);
  }

  public String getException() {
    return exception;
  }
}
