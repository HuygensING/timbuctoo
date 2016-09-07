package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class TripleProcessorImpl implements TripleProcessor {
  private static final Logger LOG = getLogger(TripleProcessorImpl.class);
  private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  private static final String RDF_SUB_CLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
  private final AddToCollectionTripleProcessor addToCollection;
  private final AddPropertyTripleProcessor addProperty;
  private final AddRelationTripleProcessor addRelation;
  private final AddToArchetypeTripleProcessor addToArchetype;
  private Database database;

  public TripleProcessorImpl(Database database) {
    this.database = database;
    this.addToCollection = new AddToCollectionTripleProcessor(database);
    this.addToArchetype = new AddToArchetypeTripleProcessor(database);
    this.addProperty = new AddPropertyTripleProcessor(database);
    this.addRelation = new AddRelationTripleProcessor(database);
  }

  private boolean subclassOfKnownArchetype(Triple triple) {
    return triple.getPredicate().getURI().equals(RDF_SUB_CLASS_OF) &&
      database.isKnownArchetype(triple.getObject().getLocalName());
  }

  private boolean objectIsNonLiteral(Triple triple) {
    return triple.getObject().isURI() || triple.getObject().isBlank();
  }

  private boolean objectIsLiteral(Triple triple) {
    return triple.getObject().isLiteral();
  }

  private boolean predicateIsType(Triple triple) {
    return triple.getPredicate().getURI().equals(RDF_TYPE);
  }

  @Override
  public void process(String vreName, Triple triple) {
    if (predicateIsType(triple)) {
      addToCollection.process(vreName, triple);
    } else if (subclassOfKnownArchetype(triple)) {
      addToArchetype.process(vreName, triple);
    } else if (objectIsLiteral(triple)) {
      addProperty.process(vreName, triple);
    } else if (objectIsNonLiteral(triple)) {
      addRelation.process(vreName, triple);
    } else {
      //This means that the object is neither a literal, nor a non-literal.
      //That would only happen if I misunderstand something
      LOG.error("Triple matches no pattern: ", triple.toString());
    }
  }
}
