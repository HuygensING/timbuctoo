package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class TripleProcessorFactory {
  static final String RDF_URI_PROP = "rdfUri";
  private GraphWrapper graphWrapper;

  public TripleProcessorFactory(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  public TripleProcessor getTripleProcessor(Triple triple) {
    if (describesType(triple)) {
      return new AddToCollectionTripleProcessor(graphWrapper);
    } else if (describesProperty(triple)) {
      return new AddPropertyTripleProcessor(graphWrapper);
    } else if (describesRelation(triple)) {
      return new AddRelationTripleProcessor(graphWrapper);
    } else {
      return new UnsupportedTripleProcessor();
    }
  }

  boolean describesRelation(Triple triple) {
    return triple.getObject().isURI();
  }

  boolean describesProperty(Triple triple) {
    return triple.getObject().isLiteral();
  }

  boolean describesType(Triple triple) {
    return triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
  }

  private static class UnsupportedTripleProcessor implements TripleProcessor {

    public UnsupportedTripleProcessor() {
    }

    @Override
    public void process(Triple triple) {
      // TODO find a better way to handle unsupported triples
      throw new IllegalArgumentException("Triple '" + triple + "' is unsupported.");
    }
  }

  private class AddToCollectionTripleProcessor implements TripleProcessor {
    private final GraphWrapper graphWrapper;
    private final CollectionMapper collectionMapper;

    public AddToCollectionTripleProcessor(GraphWrapper graphWrapper) {
      this.graphWrapper = graphWrapper;
      this.collectionMapper = new CollectionMapper(graphWrapper);
    }

    @Override
    public void process(Triple triple) {
      Node node = triple.getSubject();
      final Vertex subjectVertex = GraphUtil.findOrCreateVertex(graphWrapper.getGraph(), node);
      collectionMapper.addToCollection(subjectVertex, triple.getObject().getLocalName());
    }
  }

  private class AddPropertyTripleProcessor implements TripleProcessor {
    private final GraphWrapper graphWrapper;
    private final CollectionMapper collectionMapper;

    public AddPropertyTripleProcessor(GraphWrapper graphWrapper) {
      this.graphWrapper = graphWrapper;
      this.collectionMapper = new CollectionMapper(graphWrapper);
    }

    @Override
    public void process(Triple triple) {
      Node node = triple.getSubject();
      final Vertex subjectVertex = GraphUtil.findOrCreateVertex(graphWrapper.getGraph(), node);
      collectionMapper.addToCollection(subjectVertex, "unknown");
      subjectVertex.property(triple.getPredicate().getLocalName(), triple.getObject().getLiteralLexicalForm());
    }
  }

  private class AddRelationTripleProcessor implements TripleProcessor {
    private final GraphWrapper graphWrapper;
    private final CollectionMapper collectionMapper;

    public AddRelationTripleProcessor(GraphWrapper graphWrapper) {
      this.graphWrapper = graphWrapper;
      this.collectionMapper = new CollectionMapper(graphWrapper);
    }

    @Override
    public void process(Triple triple) {
      Node node = triple.getSubject();
      final Vertex subjectVertex = GraphUtil.findOrCreateVertex(graphWrapper.getGraph(), node);
      final Vertex objectVertex = GraphUtil.findOrCreateVertex(graphWrapper.getGraph(), triple.getObject());
      collectionMapper.addToCollection(subjectVertex, "unknown");
      collectionMapper.addToCollection(objectVertex, "unknown");
      subjectVertex.addEdge(triple.getPredicate().getLocalName(), objectVertex);
    }
  }
}
