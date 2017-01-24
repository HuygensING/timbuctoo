package nl.knaw.huygens.timbuctoo.remote.rs.view;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsItem;

import java.util.function.Function;

/**
 *
 */
public class Interpreter {

  private Function<RsItem<?>, String> itemNameInterpreter = Interpreters.locItemNameInterpreter;
  private Function<Throwable, String> errorInterpreter = Interpreters.messageErrorInterpreter;

  public Function<RsItem<?>, String> getItemNameInterpreter() {
    return itemNameInterpreter;
  }

  public Interpreter withItemNameInterpreter(Function<RsItem<?>, String> itemNameInterpreter) {
    this.itemNameInterpreter = itemNameInterpreter;
    return this;
  }

  public Function<Throwable, String> getErrorInterpreter() {
    return errorInterpreter;
  }

  public Interpreter withErrorInterpreter(Function<Throwable, String> errorInterpreter) {
    this.errorInterpreter = errorInterpreter;
    return this;
  }

  public Interpreter withStackTrace(boolean debug) {
    if (debug) {
      errorInterpreter = Interpreters.stacktraceErrorInterpreter;
    }
    return this;
  }
}
