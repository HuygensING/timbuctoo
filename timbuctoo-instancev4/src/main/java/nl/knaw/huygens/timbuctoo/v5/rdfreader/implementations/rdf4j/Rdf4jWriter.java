package nl.knaw.huygens.timbuctoo.v5.rdfreader.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadSaver;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.Writer;

public class Rdf4jWriter implements QuadSaver {
  protected final RDFWriter writer;
  protected final SimpleValueFactory valueFactory;

  public Rdf4jWriter(Writer fileWriter, RDFFormat rdfFormat) {
    writer = Rio.createWriter(rdfFormat, fileWriter);
    valueFactory = SimpleValueFactory.getInstance();
  }

  @Override
  public void start() throws LogStorageFailedException {
    writer.startRDF();
  }

  @Override
  public void onPrefix(String prefix, String iri) throws LogStorageFailedException {
    writer.handleNamespace(prefix, iri);
  }

  private Resource makeResource(String input) {
    if (input.startsWith("_:")) {
      return valueFactory.createBNode(input.substring(2));
    } else {
      return valueFactory.createIRI(input);
    }
  }


  @Override
  public void onRelation(String subject, String predicate, String object, String graph)
      throws LogStorageFailedException {
    writer.handleStatement(
      valueFactory.createStatement(
        makeResource(subject),
        valueFactory.createIRI(predicate),
        makeResource(object),
        makeResource(graph)
      )
    );
  }

  @Override
  public void onLiteral(String subject, String predicate, String value, String valueType, String graph)
      throws LogStorageFailedException {
    writer.handleStatement(
      valueFactory.createStatement(
        makeResource(subject),
        valueFactory.createIRI(predicate),
        valueFactory.createLiteral(value, valueFactory.createIRI(valueType)),
        makeResource(graph)
      )
    );
  }

  @Override
  public void onLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
      throws LogStorageFailedException {
    writer.handleStatement(
      valueFactory.createStatement(
        makeResource(subject),
        valueFactory.createIRI(predicate),
        valueFactory.createLiteral(value, language),
        makeResource(graph)
      )
    );
  }

  @Override
  public void finish() throws LogStorageFailedException {
    writer.endRDF();
  }
}
