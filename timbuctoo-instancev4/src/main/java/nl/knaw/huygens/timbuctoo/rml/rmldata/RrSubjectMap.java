package nl.knaw.huygens.timbuctoo.rml.rmldata;


import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import org.apache.jena.graph.Node_URI;

public class RrSubjectMap {
  public final RrTermMap termMap;
  public final Node_URI className;

  public RrSubjectMap(RrTermMap rrTermMap, Node_URI className) {
    this.termMap = rrTermMap;
    this.className = className;
  }

  public RrSubjectMap(RrTermMap rrTermMap) {
    this.termMap = rrTermMap;
    this.className = null;
  }

}
