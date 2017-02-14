package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import org.apache.jena.rdf.model.impl.Util;

public class RdfNameHelper {
  /**
   * Returns the last token of an RDF uri.
   */
  public static String getLocalName(String rdfUri) {
    return rdfUri.substring(Util.splitNamespaceXML(rdfUri));
  }
}
