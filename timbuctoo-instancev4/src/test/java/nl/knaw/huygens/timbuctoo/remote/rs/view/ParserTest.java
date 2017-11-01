package nl.knaw.huygens.timbuctoo.remote.rs.view;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Test;

import java.util.Optional;

/**
 * Created on 2017-10-31 11:48.
 */
public class ParserTest {

  @Test
  public void parse() throws Exception {
    // Model model = Rio.parse(IOUtils.toInputStream(createDescriptionDocument()), "", RDFFormat.RDFXML);
    // Rio.write(model, System.out, RDFFormat.JSONLD);
    //String mimeType = "application/rdf+xml";
    String mimeType = null;
    Optional<RDFFormat> maybeFormat = Rio.getParserFormatForMIMEType(mimeType);
    if (maybeFormat.isPresent()) {
      RDFFormat format = maybeFormat.get();
      System.out.println(format.getName());
    }

    String filename = "clusius3/description.xml";
    maybeFormat = Rio.getParserFormatForFileName(filename);
    System.out.println("from filename:" + maybeFormat.get());
    System.out.println(createDescriptionDocument());
  }

  private String createDescriptionDocument() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<rdf:RDF\n" +
      "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
      "\n" +
      "<rdf:Description rdf:about=\"http://example" +
      ".org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/clusius3/\">\n" +
      "\t<abstract xmlns=\"http://purl.org/dc/terms/\" rdf:resource=\"http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/clusius3/summaryProperties\"/>\n" +
      "\t<description xmlns=\"http://purl.org/dc/terms/\" rdf:datatype=\"http://www" +
      ".w3.org/2001/XMLSchema#string\">Biographical data of the Digital Web Centre for the History of Science (DWC)" +
      "</description>\n" +
      "\t<license xmlns=\"http://purl.org/dc/terms/\" rdf:resource=\"https://creativecommons" +
      ".org/publicdomain/zero/1.0/\"/>\n" +
      "\t<provenance xmlns=\"http://purl.org/dc/terms/\" rdf:resource=\"http://example" +
      ".org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/clusius3/provenance\"/>\n" +
      "\t<rightsHolder xmlns=\"http://purl.org/dc/terms/\" rdf:resource=\"http://example" +
      ".org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/clusius3/rightsHolder\"/>\n" +
      "\t<title xmlns=\"http://purl.org/dc/terms/\" rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">DWC " +
      "Data</title>\n" +
      "\t<ContactPoint xmlns=\"http://schema.org/\" rdf:resource=\"http://example" +
      ".org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/clusius3/contactPerson\"/>\n" +
      "</rdf:Description>\n" +
      "\n" +
      "</rdf:RDF>";
  }
}
