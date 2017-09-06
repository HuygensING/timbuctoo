package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.jacksonserializers.TimbuctooCustomSerializers;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class BdbTypeNameStore implements TypeNameStore {

  private static ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new Jdk8Module())
    .registerModule(new GuavaModule())
    .registerModule(new TimbuctooCustomSerializers())
    .enable(SerializationFeature.INDENT_OUTPUT);

  protected final PrefixMapping prefixMapping;
  protected final TypeNames data;
  private final DataStorage dataStore;

  public BdbTypeNameStore(DataProvider dataProvider, DataStorage dataStore) throws IOException {
    prefixMapping = new PrefixMappingImpl();
    final String storedValue = dataStore.getValue();
    if (storedValue == null) {
      data = new TypeNames();
      addStandardPrefixes(data);
    } else {
      data = objectMapper.readValue(storedValue, new TypeReference<TypeNames>() {});
    }
    prefixMapping.setNsPrefixes(data.prefixes);
    this.dataStore = dataStore;
    dataProvider.subscribeToRdf(new Subscription());
  }

  //I think that a fully reversable shortened version looks ugly. And usually this is not needed
  //So I shorten by throwing away information and use a HashMap to be able to revert the process
  //and prevent collisions.
  @Override
  public String makeGraphQlname(String uri) {
    return makeName(uri, "");
  }

  @Override
  public String makeGraphQlnameForPredicate(String uri, Direction direction) {
    return makeName(uri, direction == Direction.IN ? "_inverse_" : "");
  }

  public String makeName(String uri, String prefix) {
    //The relay spec requires that our own names are never 'PageInfo' or end with 'Connection'

    if (data.shorteneds.containsKey(prefix + "\n" + uri)) {
      return data.shorteneds.get(prefix + "\n" + uri);
    } else {
      String shortened = prefix + shorten(uri).replaceAll("[^_0-9A-Za-z]", "_");
      while (shortened.equals("PageInfo") ||
        shortened.endsWith("Connection") ||
        shortened.endsWith("Edge") ||
        data.inverse.containsKey(shortened)) {

        shortened += "_";
      }
      data.shorteneds.put(prefix + "\n" + uri, shortened);
      data.inverse.put(shortened, uri);
      return shortened;
    }
  }

  @Override
  public String makeGraphQlValuename(String uri) {
    return makeName(uri, "value_");
  }

  @Override
  public String makeUri(String graphQlName) {
    return data.inverse.get(graphQlName);
  }

  @Override
  public Optional<Tuple<String, Direction>> makeUriForPredicate(String graphQlName) {
    String uri = makeUri(graphQlName);
    if (uri == null) {
      return Optional.empty();
    } else {
      if (graphQlName.startsWith("_inverse_")) {
        return Optional.of(Tuple.tuple(uri, Direction.IN));
      } else {
        return Optional.of(Tuple.tuple(uri, Direction.OUT));
      }
    }
  }

  @Override
  public String shorten(String uri) {
    return prefixMapping.shortForm(uri);
  }

  @Override
  public Map<String, String> getMappings() {
    return prefixMapping.getNsPrefixMap();
  }

  private void addPrefix(TypeNames typeNames, String prefix, String iri) {
    typeNames.prefixes.put(prefix, iri);
    prefixMapping.setNsPrefix(prefix, iri); //idempotent
  }

  @Override
  public void close() throws IOException {
    try {
      dataStore.setValue(objectMapper.writeValueAsString(data));
      dataStore.close();
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  private class Subscription implements RdfProcessor {

    private int currentVersion = -1;

    @Override
    public void setPrefix(String prefix, String iri) throws RdfProcessingFailedException {
      addPrefix(data, prefix, iri);
    }

    @Override
    public void addRelation(String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
      // no implementation needed
    }

    @Override
    public void addValue(String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
      // no implementation needed
    }

    @Override
    public void addLanguageTaggedString(String subject, String predicate, String value, String language,
                                        String graph) throws RdfProcessingFailedException {
      // no implementation needed
    }

    @Override
    public void delRelation(String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
      // no implementation needed
    }

    @Override
    public void delValue(String subject, String predicate, String value, String valueType, String graph)
      throws RdfProcessingFailedException {
      // no implementation needed
    }

    @Override
    public void delLanguageTaggedString(String subject, String predicate, String value, String language,
                                        String graph) throws RdfProcessingFailedException {
      // no implementation needed
    }

    @Override
    public void start(int index) throws RdfProcessingFailedException {
      // no implementation needed
      currentVersion = index;
    }

    @Override
    public int getCurrentVersion() {
      return currentVersion;
    }

    @Override
    public void commit() throws RdfProcessingFailedException {
      try {
        dataStore.setValue(objectMapper.writeValueAsString(data));
      } catch (IOException | DatabaseWriteException e) {
        throw new RdfProcessingFailedException(e);
      }
    }
  }

  public void addStandardPrefixes(TypeNames typeNames) {
    addPrefix(typeNames, "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    addPrefix(typeNames, "foaf", "http://xmlns.com/foaf/0.1/");
    addPrefix(typeNames, "rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    addPrefix(typeNames, "owl", "http://www.w3.org/2002/07/owl#");
    addPrefix(typeNames, "skos", "http://www.w3.org/2004/02/skos/core#");
    addPrefix(typeNames, "dcat", "http://www.w3.org/ns/dcat#");
    addPrefix(typeNames, "xsd", "http://www.w3.org/2001/XMLSchema#");
    addPrefix(typeNames, "prov", "http://www.w3.org/ns/prov#");
    addPrefix(typeNames, "dcterms", "http://purl.org/dc/terms/");
    addPrefix(typeNames, "dbpedia", "http://dbpedia.org/resource/");
    addPrefix(typeNames, "schema", "http://schema.org/");
    addPrefix(typeNames, "tim", "http://timbuctoo.huygens.knaw.nl/v5/vocabulary#");
    addPrefix(typeNames, "timdata", "http://timbuctoo.huygens.knaw.nl/v5/data/");
  }

}
