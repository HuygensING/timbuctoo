package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.knaw.huygens.timbuctoo.search.EntityRef;

import java.util.List;

public interface Resultable {

  Long getResultCount();

  List<EntityRef> getResults();

}
