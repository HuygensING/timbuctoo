package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfProperty;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfReadProperty;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class PersonNamesTripleProcessor extends AbstractValueTripleProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(PropertyTripleProcessor.class);
  private final RdfImportSession rdfImportSession;
  private final ObjectMapper objectMapper;

  public PersonNamesTripleProcessor(RdfImportSession rdfImportSession) {
    this.rdfImportSession = rdfImportSession;
    objectMapper = new ObjectMapper();
  }

  @Override
  protected void processAssertion(String vreName, String subject, String predicate, String lexicalValue,
                                  String typeUri) {
    Optional<RdfReadProperty> rdfPropertyOpt = rdfImportSession.retrieveProperty(subject, predicate);
    try {
      PersonName personName = objectMapper.readValue(lexicalValue, PersonName.class);
      PersonNames personNames =
        getPersonNames(objectMapper, rdfPropertyOpt);
      personNames.list.add(personName);
      String names = objectMapper.writeValueAsString(personNames);
      rdfImportSession.assertProperty(subject, new RdfProperty(predicate, names, typeUri));
    } catch (IOException e) {
      LOG.error("Could not convert '{}' to PersonName", lexicalValue);
    }
  }

  private PersonNames getPersonNames(ObjectMapper objectMapper, Optional<RdfReadProperty> rdfPropertyOpt)
    throws IOException {
    if (rdfPropertyOpt.isPresent()) {
      return objectMapper.readValue(rdfPropertyOpt.get().getValue(), PersonNames.class);
    } else {
      return new PersonNames();
    }
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, String lexicalValue,
                                   String typeUri) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
