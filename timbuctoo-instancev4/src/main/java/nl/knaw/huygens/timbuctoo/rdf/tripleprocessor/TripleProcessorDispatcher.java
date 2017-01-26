package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;

import java.util.Set;

import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleParser.fromTriple;
import static org.slf4j.LoggerFactory.getLogger;

public class TripleProcessorDispatcher {
  static final String TIM_IS_NAME_VARIANT_OF = "http://timbuctoo.huygens.knaw.nl/isNameVariantOf";
  private static final Logger LOG = getLogger(TripleProcessorDispatcher.class);
  private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  private static final String RDF_SUB_CLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
  private static final String OWL_SAME_AS = "http://www.w3.org/2002/07/owl#sameAs";
  private static final String SKOS_ALT_LABEL = "http://www.w3.org/2004/02/skos/core#altLabel";
  private static final String TEI_NAMESPACE = "http://www.tei-c.org/ns/1.0/";
  private static final Set<String> TEI_NAMES_COMPONENTS = Sets.newHashSet(
    "surname", "forename", "genName", "roleName", "addName", "nameLink"
  );

  private final CollectionMembershipTripleProcessor collectionMembership;
  private final PropertyTripleProcessor property;
  private final RelationTripleProcessor relation;
  private final ArchetypeTripleProcessor archetype;
  private final SameAsTripleProcessor sameAs;
  private final AltLabelTripleProcessor altLabel;
  private final PersonNamesTripleProcessor personNames;

  private Database database;
  private PersonNameVariantTripleProcessor personNameVariant;

  public TripleProcessorDispatcher(Database database, RdfImportSession rdfImportSession) {
    this.database = database;
    this.collectionMembership = new CollectionMembershipTripleProcessor(database, rdfImportSession);
    this.archetype = new ArchetypeTripleProcessor(database);
    this.property = new PropertyTripleProcessor(rdfImportSession);
    this.relation = new RelationTripleProcessor(database);
    this.sameAs = new SameAsTripleProcessor(database);
    this.altLabel = new AltLabelTripleProcessor(database);
    this.personNames = new PersonNamesTripleProcessor(database);
    this.personNameVariant = new PersonNameVariantTripleProcessor(database);
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

  private boolean predicateIsSameAs(Triple triple) {
    return triple.getPredicate().getURI().equals(OWL_SAME_AS);
  }

  private boolean predicateIsAltLabel(Triple triple) {
    return triple.getPredicate().getURI().equals(SKOS_ALT_LABEL);
  }

  private boolean predicateIsTeiName(Triple triple) {
    return triple.getPredicate().getNameSpace().equals(TEI_NAMESPACE) &&
      TEI_NAMES_COMPONENTS.contains(triple.getPredicate().getLocalName());
  }

  private boolean predicateIsNameVariant(Triple triple) {
    return triple.getPredicate().getURI().equals(TIM_IS_NAME_VARIANT_OF);
  }

  //FIXME: add unittests for isAssertion
  public void dispatch(String vreName, boolean isAssertion, Triple triple) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(vreName + (isAssertion ? ": + " : ": - ") + triple);
    }
    TripleParser tripleParser = fromTriple(triple);
    String subject = tripleParser.getSubjectReference();
    String predicate = tripleParser.getPredicateReference();

    if (predicateIsType(triple)) {
      collectionMembership.process(vreName, subject, predicate, tripleParser.getObjectReference(), isAssertion);
    } else if (predicateIsSameAs(triple)) {
      sameAs.process(vreName, isAssertion, triple);
    } else if (subclassOfKnownArchetype(triple)) {
      archetype.process(vreName, subject, predicate, tripleParser.getObjectReference(), isAssertion);
    } else if (predicateIsAltLabel(triple)) {
      altLabel.process(vreName, isAssertion, triple);
    } else if (predicateIsTeiName(triple)) {
      personNames.process(vreName, isAssertion, triple);
    } else if (predicateIsNameVariant(triple)) {
      personNameVariant.process(vreName, isAssertion, triple);
    } else if (objectIsLiteral(triple)) {
      property.process(vreName, isAssertion, triple);
    } else if (objectIsNonLiteral(triple)) {
      relation.process(vreName, subject, predicate, tripleParser.getObjectReference(), isAssertion);
    } else {
      //This means that the object is neither a literal, nor a non-literal.
      //That would only happen if I misunderstand something
      LOG.error("Triple matches no pattern: ", triple.toString());
    }
  }

}
