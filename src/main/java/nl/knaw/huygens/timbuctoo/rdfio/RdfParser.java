package nl.knaw.huygens.timbuctoo.rdfio;

import nl.knaw.huygens.timbuctoo.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingParseException;
import nl.knaw.huygens.timbuctoo.filestorage.dto.CachedLog;

public interface RdfParser {
  void importRdf(CachedLog input, String baseUri, String defaultGraph,
                 RdfProcessor rdfProcessor) throws RdfProcessingFailedException, RdfProcessingParseException;
}
