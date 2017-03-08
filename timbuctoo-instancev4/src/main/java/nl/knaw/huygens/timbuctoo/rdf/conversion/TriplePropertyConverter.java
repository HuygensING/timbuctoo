package nl.knaw.huygens.timbuctoo.rdf.conversion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.core.PropertyConverter;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.core.dto.property.AltNamesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.ArrayOfLimitedValuesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.ArrayProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.DatableProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.DefaultFullPersonNameProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.DefaultLocationNameProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.EncodedStringOfLimitedValuesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.HyperLinksProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.PersonNamesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.StringOfLimitedValuesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.StringProperty;
import nl.knaw.huygens.timbuctoo.rdf.LinkTriple;
import nl.knaw.huygens.timbuctoo.rdf.LiteralTriple;
import nl.knaw.huygens.timbuctoo.rdf.Triple;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class TriplePropertyConverter extends PropertyConverter<List<Triple>> {
  private final String subjectUri;
  private final ObjectMapper objectMapper;

  public TriplePropertyConverter(Collection collection, String subjectUri) {
    super(collection);
    this.subjectUri = subjectUri;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  protected ArrayProperty createArrayProperty(String propertyName, List<Triple> value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected AltNamesProperty createAltNamesProperty(String propertyName, List<Triple> value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected DatableProperty createDatableProperty(String propertyName, List<Triple> value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected DefaultFullPersonNameProperty createDefaultFullPersonNameProperty(String propertyName, List<Triple> value)
    throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected DefaultLocationNameProperty createDefaultLocationNameProperty(String propertyName, List<Triple> value)
    throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected HyperLinksProperty createHyperLinksProperty(String propertyName, List<Triple> value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected PersonNamesProperty createPersonNamesProperty(String propertyName, List<Triple> value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected ArrayOfLimitedValuesProperty createArrayOfLimitedValuesProperty(String propertyName, List<Triple> value)
    throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected EncodedStringOfLimitedValuesProperty createEncodedStringOfLimitedValuesProperty(String propertyName,
                                                                                            List<Triple> value)
    throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected StringProperty createStringProperty(String propertyName, List<Triple> value) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected StringOfLimitedValuesProperty createStringOfLimitedValues(String propertyName, List<Triple> value)
    throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Tuple<String, List<Triple>> to(AltNamesProperty property) throws IOException {
    // TODO find a better way to serialize to n-triples
    String objectValue = objectMapper.writeValueAsString(property.getValue());
    return tuple(property.getName(),
      Lists.newArrayList(new LiteralTriple(
        subjectUri,
        createMetadata(property.getName()),
        objectValue,
        createMetadata("altnames")
      ))
    );
  }

  @Override
  public Tuple<String, List<Triple>> to(DatableProperty property) throws IOException {
    return tuple(property.getName(),
      Lists.newArrayList(new LiteralTriple(
        subjectUri,
        createMetadata(property.getName()),
        property.getValue(),
        "https://www.loc.gov/standards/datetime/pre-submission.html"
      ))
    );
  }

  @Override
  public Tuple<String, List<Triple>> to(DefaultFullPersonNameProperty property) throws IOException {
    return tuple(property.getName(), Lists.newArrayList());
  }

  @Override
  public Tuple<String, List<Triple>> to(DefaultLocationNameProperty property) throws IOException {
    return tuple(property.getName(), Lists.newArrayList());
  }

  @Override
  public Tuple<String, List<Triple>> to(HyperLinksProperty property) throws IOException {
    String blankNode = "_:" + UUID.randomUUID();
    String propertyName = property.getName();
    String pred = createMetadata(propertyName);
    List<Triple> triples = Lists.newArrayList(new LinkTriple(subjectUri, pred, blankNode));
    JsonNode jsonNode = objectMapper.readTree(property.getValue());
    for (Iterator<JsonNode> elements = jsonNode.elements(); elements.hasNext(); ) {
      JsonNode jn = elements.next();
      jn.fieldNames().forEachRemaining(field -> {
        String predicate = pred + field;
        String fieldValue = jn.get(field).asText();
        String object = StringUtils.isBlank(fieldValue) ? "\"\"" : fieldValue;
        triples.add(new LinkTriple(blankNode, predicate, object));
      });
    }

    return tuple(propertyName, triples);
  }

  @Override
  public Tuple<String, List<Triple>> to(PersonNamesProperty property) throws IOException {
    // TODO find a better way to serialize to n-triples
    String objectValue = objectMapper.writeValueAsString(property.getValue());
    return tuple(
      property.getName(),
      Lists.newArrayList(new LiteralTriple(
        subjectUri,
        createMetadata(property.getName()),
        objectValue,
        createMetadata("personnames")
      ))
    );
  }

  @Override
  public Tuple<String, List<Triple>> to(ArrayOfLimitedValuesProperty property) throws IOException {
    return mapArrayProperty(property.getName(), property.getValue());
  }

  @Override
  public Tuple<String, List<Triple>> to(EncodedStringOfLimitedValuesProperty property) throws IOException {
    return tuple(
      property.getName(),
      Lists.newArrayList(new LiteralTriple(
        subjectUri,
        createMetadata(property.getName()),
        property.getValue(),
        "http://www.w3.org/2001/XMLSchema#string"
      ))
    );
  }

  @Override
  public Tuple<String, List<Triple>> to(StringProperty property) throws IOException {
    return tuple(
      property.getName(),
      Lists.newArrayList(new LiteralTriple(
        subjectUri,
        createMetadata(property.getName()),
        property.getValue(),
        "http://www.w3.org/2001/XMLSchema#string"
      ))
    );
  }

  @Override
  public Tuple<String, List<Triple>> to(StringOfLimitedValuesProperty property) throws IOException {
    return tuple(property.getName(),
      Lists.newArrayList(new LiteralTriple(
        subjectUri,
        createMetadata(property.getName()),
        property.getValue(),
        "http://www.w3.org/2001/XMLSchema#string"
      ))
    );
  }

  @Override
  public Tuple<String, List<Triple>> to(ArrayProperty property) throws IOException {
    return mapArrayProperty(property.getName(), property.getValue());

  }

  private String createMetadata(String metadataName) {
    // FIXME find a way to look up the predicate of a property
    return String.format("http://timbuctoo.huygens.knaw.nl/%s", metadataName);
  }

  private Tuple<String, List<Triple>> mapArrayProperty(String propertyName, String value) throws IOException {
    List<String> values = objectMapper.readValue(value, new TypeReference<List<String>>() {
    });
    List<Triple> collect = values.stream()
      .map(v -> new LiteralTriple(
        subjectUri,
        createMetadata(propertyName),
        v,
        "http://www.w3.org/2001/XMLSchema#string"
      ))
      .collect(Collectors.toList());

    return tuple(propertyName, collect);
  }
}
