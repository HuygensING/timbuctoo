package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class SameAsTripleProcessor {
  private final Database database;
  private static final Logger LOG = getLogger(SameAsTripleProcessor.class);

  public SameAsTripleProcessor(Database database) {
    this.database = database;
  }

  public void process(String vreName, boolean isAssertion, Triple triple) {
    Optional<Entity> object = database.findEntity(vreName, triple.getObject());
    Optional<Entity> subject = database.findEntity(vreName, triple.getSubject());
    if (object.isPresent() && subject.isPresent()) {
      //FIXME: handle this case
      //FIXME: handle the case where the same property is asserted for both same-as versions using different values
      LOG.error("SameAs encountered, but the entities both already exist. This is not handled yet");
    } else if (object.isPresent()) {
      database.addRdfSynonym(vreName, object.get(), triple.getSubject());
    } else if (subject.isPresent()) {
      database.addRdfSynonym(vreName, subject.get(), triple.getObject());
    } else {
      Entity entity = database.findOrCreateEntity(vreName, triple.getObject());
      database.addRdfSynonym(vreName, entity, triple.getSubject());
    }
  }
}
