package nl.knaw.huygens.timbuctoo.v5.logprocessing.dto;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfreader.RdfParser;

import java.net.URI;

/**
 * In interface that represents a log file that is already stored locally and can be repeatedly
 * read from.
 */
public interface LocalLog {
  /**
   * The worldwide unique name for this log (an iri, ending with the original filename, but does not necessarily point
   * to the file)
   */
  URI getName();

  /**
   * Load all quads from the log
   *
   * <p>This method will be called more then once. It is implemented on LocalLog so that we can
   * abstract the details of making multiple iterations work (caching etc)
   */
  void loadQuads(RdfParser rdfParser, QuadHandler quadHandler) throws LogProcessingFailedException;

}
