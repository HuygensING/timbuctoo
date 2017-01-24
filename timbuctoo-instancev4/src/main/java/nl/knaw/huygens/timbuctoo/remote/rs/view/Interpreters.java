package nl.knaw.huygens.timbuctoo.remote.rs.view;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsItem;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Base64;
import java.util.function.Function;

/**
 *
 */
public class Interpreters {


  public static Function<RsItem<?>, String> locItemNameInterpreter = RsItem::getLoc;

  public static Function<RsItem<?>, String> base64EncodedItemNameInterpreter = (rsItem) -> {
    String loc = rsItem.getLoc();
    String[] parts = loc.split("/");
    String directory = parts[parts.length - 2];
    String itemName = null;
    try {
      itemName = new String(Base64.getUrlDecoder().decode(directory.getBytes())).replace("\n", "");
    } catch (IllegalArgumentException e) {
      itemName = ">> Error: " + e.getClass().getName() + ": Unable to decode '" + directory + "'. " + e.getMessage();
    }
    return itemName;
  };

  public static Function<Throwable, String> messageErrorInterpreter = Throwable::getMessage;

  public static Function<Throwable, String> classAndMessageErrorInterpreter = (throwable) -> {
    return throwable.getClass().getName() + ": " + throwable.getMessage();
  };

  public static Function<Throwable, String> stacktraceErrorInterpreter = ExceptionUtils::getStackTrace;
}
