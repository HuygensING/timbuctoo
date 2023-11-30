package nl.knaw.huygens.timbuctoo.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.rdf4j.model.BNode;

import static org.eclipse.rdf4j.model.util.Values.bnode;

public class BNodeHelper {
  private static final String BNODE_PATTERN = ".well-known/genid/";

  public static boolean isSkolomIri(String iri) {
    return iri.contains(BNODE_PATTERN);
  }

  public static String createSkolomIri(String baseUri, String fileName, BNode resource) {
    String nodeName = resource.toString();
    String nodeId = nodeName.substring(nodeName.indexOf(":") + 1);
    return baseUri + BNODE_PATTERN + DigestUtils.md5Hex(fileName) + "_" + nodeId;
  }

  public static BNode createBNode(String iri) {
    return bnode(iri.substring(iri.indexOf(BNODE_PATTERN) + BNODE_PATTERN.length()));
  }
}
