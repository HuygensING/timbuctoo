package nl.knaw.huygens.timbuctoo.contractdiff.jsondiff;

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.timbuctoo.contractdiff.diffresults.DiffResult;

public interface Recurser {
  DiffResult recurser(JsonNode actual, JsonNode expected);
}
