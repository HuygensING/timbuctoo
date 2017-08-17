package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfParser;

import java.io.IOException;


public class Rdf4jNQuadUdParser implements RdfParser {

  @Override
  public void importRdf(String cursorPrefix, String startFrom, CachedLog input, RdfProcessor rdfProcessor)
    throws RdfProcessingFailedException {

    NquadsUdParser nquadsUdParser = new NquadsUdParser();
    int startFromInt = startFrom.isEmpty() ? 0 : Integer.parseInt(startFrom);
    nquadsUdParser.setRDFHandler(new NquadsUdHandler(rdfProcessor, input.getName(), cursorPrefix, startFromInt));

    try {
      nquadsUdParser.parse(input.getReader(), input.getName());
    } catch (IOException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

}
