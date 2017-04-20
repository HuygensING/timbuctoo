package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

import java.util.HashMap;
import java.util.Map;

public class HardCodedTypeNameStore implements TypeNameStore {
  PrefixMapping prefixMapping = new PrefixMappingImpl();
  Map<String, String> shorteneds = new HashMap<>();
  Map<String, String> inverse = new HashMap<>();

  public HardCodedTypeNameStore(String dataSetName) {
    prefixMapping.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
    prefixMapping.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    prefixMapping.setNsPrefix("terms", "http://www.openarchives.org/ore/terms/");
    prefixMapping.setNsPrefix("edm", "http://www.europeana.eu/schemas/edm/");
    prefixMapping.setNsPrefix("p-plan", "http://purl.org/net/p-plan#");
    prefixMapping.setNsPrefix("bgn", "http://data.biographynet.nl/rdf/");
    prefixMapping.setNsPrefix("tim", "http://timbuctoo.huygens.knaw.nl/v5#");
    prefixMapping.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
  }

  //I think that a fully reversable shortened version looks ugly. And usually this is not needed
  //So I shorten by throwing away information and use a HashMap to be able to revert the process
  //and prevent collisions.
  @Override
  public String makeGraphQlname(String uri) {
    if (shorteneds.containsKey(uri)) {
      return shorteneds.get(uri);
    } else {
      String shortened = shorten(uri).replaceAll("[^_0-9A-Za-z]", "_");
      while (shorteneds.containsKey(shortened)) {
        shortened += "_";
      }
      shorteneds.put(uri, shortened);
      inverse.put(shortened, uri);
      return shortened;
    }
  }

  @Override
  public String makeUri(String graphQlName) {
    return inverse.getOrDefault(graphQlName, graphQlName);
  }

  @Override
  public String shorten(String uri) {
    return prefixMapping.shortForm(uri);
  }

  @Override
  public Map<String, String> getMappings() {
    return prefixMapping.getNsPrefixMap();
  }

  @Override
  public void close() throws Exception {

  }
}
