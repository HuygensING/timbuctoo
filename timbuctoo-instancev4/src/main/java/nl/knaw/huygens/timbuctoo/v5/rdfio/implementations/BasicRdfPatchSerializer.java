package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations;

import com.google.common.base.Charsets;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;

import javax.ws.rs.core.MediaType;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

public class BasicRdfPatchSerializer implements RdfPatchSerializer {

  String results = "";
  private final PrintWriter printWriter;

  public BasicRdfPatchSerializer(OutputStream output) {
    printWriter = new PrintWriter(new OutputStreamWriter(output, Charsets.UTF_8),true);
  }

  public String getResults() {
    return results;
  }

  @Override
  public void delRelation(String subject, String predicate, String object, String graph)
    throws LogStorageFailedException {
    printWriter.write("-" + "<" + subject + "> <" + predicate + "> " + object + " .\n");
  }

  @Override
  public void delValue(String subject, String predicate, String value, String valueType, String graph)
    throws LogStorageFailedException {
    printWriter.write("-" + "<" + subject + "> <" + predicate + "> " + value + " .\n");
  }

  @Override
  public void delLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
    throws LogStorageFailedException {
    printWriter.write("-" + "<" + subject + "> <" + predicate + "> " + value + " .\n");
  }

  @Override
  public MediaType getMediaType() {
    return new MediaType("application", "vnd.timbuctoo-rdf.nquads_unified_diff");

  }

  @Override
  public Charset getCharset() {
    return Charsets.UTF_8;
  }

  @Override
  public void onPrefix(String prefix, String iri) throws LogStorageFailedException {

  }

  @Override
  public void onRelation(String subject, String predicate, String object, String graph)
    throws LogStorageFailedException {
    printWriter.write("+" + "<" + subject + "> <" + predicate + "> " + object + " .\n");
  }

  @Override
  public void onValue(String subject, String predicate, String value, String valueType, String graph)
    throws LogStorageFailedException {
    printWriter.write("+" + "<" + subject + "> <" + predicate + "> " + value + " .\n");
  }

  @Override
  public void onLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
    throws LogStorageFailedException {

  }

  @Override
  public void close() throws LogStorageFailedException {
    printWriter.flush();
    printWriter.close();
  }
}
