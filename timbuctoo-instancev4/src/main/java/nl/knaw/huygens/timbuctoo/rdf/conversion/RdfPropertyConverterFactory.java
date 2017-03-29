package nl.knaw.huygens.timbuctoo.rdf.conversion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import nl.knaw.huygens.timbuctoo.core.dto.property.AltNamesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.ArrayOfLimitedValuesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.ArrayProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.DatableProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.EncodedStringOfLimitedValuesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.HyperLinksProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.PersonNamesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.StringOfLimitedValuesProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.StringProperty;
import nl.knaw.huygens.timbuctoo.core.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.model.AltNames;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.rethrowConsumer;

public class RdfPropertyConverterFactory {

  public static final Logger LOG = LoggerFactory.getLogger(RdfPropertyConverterFactory.class);
  private final ObjectMapper objectMapper;

  public RdfPropertyConverterFactory() {
    objectMapper = new ObjectMapper();
  }

  public RdfPropertyConverter getConverter(String rdfPredicate, String rdfDataType) {
    switch (rdfDataType) {
      case "http://timbuctoo.huygens.knaw.nl/datatypes/altnames":
        return new RdfPropertyConverter() {
          @Override
          public String getPropertyType() {
            return "altnames";
          }

          @Override
          public TimProperty<?> convert(String rdfValue) throws IOException {
            return new AltNamesProperty(null, objectMapper.readValue(rdfValue, AltNames.class));
          }
        };
      case "http://timbuctoo.huygens.knaw.nl/datatypes/datable":
        return new RdfPropertyConverter() {
          @Override
          public String getPropertyType() {
            return "datable";
          }

          @Override
          public TimProperty<?> convert(String rdfValue) throws IOException {
            return new DatableProperty(null, rdfValue);
          }
        };
      case "http://timbuctoo.huygens.knaw.nl/datatypes/hyperlinks":
        return new RdfPropertyConverter() {
          @Override
          public String getPropertyType() {
            return "hyperlinks";
          }

          @Override
          public TimProperty<?> convert(String rdfValue) throws IOException {
            JsonNode value = objectMapper.valueToTree(rdfValue);
            if (value instanceof ArrayNode) {
              value.forEach(rethrowConsumer(val -> {
                if (!val.isObject()) {
                  throw new IOException("each item in the array should be an object node");
                } else {
                  if (!val.has("url") || !val.get("url").isTextual()) {
                    throw new IOException("each item in the array must have an url property containing a string");
                  }
                  if (!val.has("label") || !val.get("url").isTextual()) {
                    throw new IOException("each item in the array must have an url property containing a string");
                  }
                }
              }));
              return new HyperLinksProperty(null, objectMapper.writeValueAsString(value));
            }
            throw new IOException(String.format("'%s' should be an array of hyperlinks.", null));
          }
        };
      case "http://timbuctoo.huygens.knaw.nl/datatypes/person-names":
        return new RdfPropertyConverter() {
          @Override
          public String getPropertyType() {
            return "person-names";
          }

          @Override
          public TimProperty<?> convert(String rdfValue) throws IOException {
            return new PersonNamesProperty(null, objectMapper.readValue(rdfValue, PersonNames.class));
          }
        };
      case "http://timbuctoo.huygens.knaw.nl/datatypes/encoded-array":
        return new RdfPropertyConverter() {
          @Override
          public String getPropertyType() {
            return "encode-array";
          }

          @Override
          public TimProperty<?> convert(String rdfValue) throws IOException {
            return new ArrayProperty(null, objectMapper.writeValueAsString(rdfValue));
          }
        };
      case "http://timbuctoo.huygens.knaw.nl/datatypes/encoded-array-of-limited-values":
        return new RdfPropertyConverter() {
          @Override
          public String getPropertyType() {
            return "encoded-array-of-limited-values";
          }

          @Override
          public TimProperty<?> convert(String rdfValue) throws IOException {
            return new ArrayOfLimitedValuesProperty(null, objectMapper.writeValueAsString(rdfValue));
          }
        };
      case "http://timbuctoo.huygens.knaw.nl/datatypes/encoded-string-of-limited-values":
        return new RdfPropertyConverter() {
          @Override
          public String getPropertyType() {
            return "encoded-string-of-limited-values";
          }

          @Override
          public TimProperty<?> convert(String rdfValue) throws IOException {
            return new EncodedStringOfLimitedValuesProperty(null, objectMapper.writeValueAsString(rdfValue));
          }
        };
      case "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString":
      case "http://www.w3.org/2001/XMLSchema#string":
      case "http://timbuctoo.huygens.knaw.nl/datatypes/string":
        return new StringRdfPropertyConverter();
      case "http://timbuctoo.huygens.knaw.nl/datatypes/unencoded-string-of-limited-values":
        return new RdfPropertyConverter() {
          @Override
          public String getPropertyType() {
            return "unencoded-string-of-limited-values";
          }

          @Override
          public TimProperty<?> convert(String rdfValue) throws IOException {
            return new StringOfLimitedValuesProperty(null, rdfValue);
          }
        };
      default:
        LOG.warn("'{}' unknown rdf data type using the the default string converter", rdfDataType);
        return new StringRdfPropertyConverter();
    }
  }

  public interface RdfPropertyConverter {
    String getPropertyType();

    TimProperty<?> convert(String rdfValue) throws IOException;
  }

  private static class StringRdfPropertyConverter implements RdfPropertyConverter {

    public StringRdfPropertyConverter() {
    }

    @Override
    public String getPropertyType() {
      return "string";
    }

    @Override
    public TimProperty<?> convert(String rdfValue) throws IOException {
      return new StringProperty(null, rdfValue);
    }
  }
}
