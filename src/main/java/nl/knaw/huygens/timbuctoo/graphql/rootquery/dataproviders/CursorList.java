package nl.knaw.huygens.timbuctoo.graphql.rootquery.dataproviders;

import com.google.common.base.Charsets;
import org.immutables.value.Value;

import java.util.Base64;
import java.util.Optional;

public interface CursorList {
  Base64.Encoder ENCODER = Base64.getEncoder();

  Optional<String> getPrevCursor();

  Optional<String> getNextCursor();

  static String encode(String prevCursor) {
    if (prevCursor == null) {
      return null;
    } else {
      return ENCODER.encodeToString(prevCursor.getBytes(Charsets.UTF_8));
    }
  }
}
