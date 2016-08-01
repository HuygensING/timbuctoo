package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.util.Tuple;

public interface TripleMapGetter {
  Tuple<Integer, RrTriplesMap> getTriplesMap(String uri);
}
