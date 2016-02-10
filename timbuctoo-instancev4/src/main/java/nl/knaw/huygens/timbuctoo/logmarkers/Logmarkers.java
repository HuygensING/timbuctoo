package nl.knaw.huygens.timbuctoo.logmarkers;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Logmarkers {
  public static Marker databaseInvariant = MarkerFactory.getMarker("Database_invariant_not_upheld");
  public static Marker serviceUnavailable = MarkerFactory.getMarker("Service_unavailable");
}
