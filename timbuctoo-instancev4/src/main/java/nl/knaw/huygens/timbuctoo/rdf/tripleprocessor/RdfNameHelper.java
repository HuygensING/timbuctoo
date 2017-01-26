package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import org.apache.jena.rdf.model.impl.Util;

public class RdfNameHelper {
  public static String getEntityTypeName(String rdfUri) {
    // We use the local name from the object of a type triple as the entity type name of a timbuctoo collection.
    return rdfUri.substring(Util.splitNamespaceXML(rdfUri));
  }
}
