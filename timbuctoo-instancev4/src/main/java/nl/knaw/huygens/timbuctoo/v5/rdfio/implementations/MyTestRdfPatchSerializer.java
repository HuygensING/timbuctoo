package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations;

import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;

import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;

public class MyTestRdfPatchSerializer implements RdfPatchSerializer {

  String results = "";

  public String getResults() {
    return results;
  }

  @Override
  public void delRelation(String subject, String predicate, String object, String graph)
    throws LogStorageFailedException {
    results += "- " + subject + " " + predicate + " " + object + "\n";
  }

  @Override
  public void delValue(String subject, String predicate, String value, String valueType, String graph)
    throws LogStorageFailedException {
    results += "- " + subject + " " + predicate + " " + value + "\n";
  }

  @Override
  public void delLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
    throws LogStorageFailedException {

  }

  @Override
  public MediaType getMediaType() {
    return null;
  }

  @Override
  public Charset getCharset() {
    return null;
  }

  @Override
  public void onPrefix(String prefix, String iri) throws LogStorageFailedException {

  }

  @Override
  public void onRelation(String subject, String predicate, String object, String graph)
    throws LogStorageFailedException {
    results += "+ " + subject + " " + predicate + " " + object + "\n";
  }

  @Override
  public void onValue(String subject, String predicate, String value, String valueType, String graph)
    throws LogStorageFailedException {
    results += "+ " + subject + " " + predicate + " " + value + "\n";
  }

  @Override
  public void onLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
    throws LogStorageFailedException {

  }

  @Override
  public void close() throws LogStorageFailedException {

  }
}
