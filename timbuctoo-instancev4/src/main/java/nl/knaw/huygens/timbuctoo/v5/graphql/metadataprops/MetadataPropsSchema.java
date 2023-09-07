package nl.knaw.huygens.timbuctoo.v5.graphql.metadataprops;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.EntityMetadataProp;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.MetadataProp;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.SimpleMetadataProp;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.UriMetadataProp;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class MetadataPropsSchema {
  private final SchemaParser schemaParser;
  private final Map<String, MetadataProp> metadataProps;

  public MetadataPropsSchema(SchemaParser schemaParser, Map<String, MetadataProp> metadataProps) {
    this.schemaParser = schemaParser;
    this.metadataProps = metadataProps;
  }

  public TypeDefinitionRegistry getTypeDefinition() {
    String schema = getPropTypes(metadataProps, true) +
        "extend type DataSetMetadata {\n" +
        getReadProps(metadataProps) +
        "}\n" +
        "\n" +
        "extend type Mutation {\n" +
        "  setDataSetMetadata(dataSetId: String!, metadata: DataSetMetadataInput!): DataSetMetadata!\n" +
        "}\n" +
        "\n";

    return schemaParser.parse(schema);
  }

  private static String getPropTypes(Map<String, MetadataProp> props, boolean isRoot) {
    final StringBuilder propTypes = new StringBuilder();

    if (isRoot) {
      propTypes.append("input DataSetMetadataInput {\n");
      props.forEach((name, prop) -> propTypes.append(createWriteProp(name, prop)));
      propTypes.append("}\n\n");
    }

    props.forEach((name, prop) -> {
      if (prop instanceof EntityMetadataProp) {
        propTypes.append("type ").append(StringUtils.capitalize(name)).append(" {\n");
        ((EntityMetadataProp) prop).getProperties().forEach((childName, childProp) ->
            propTypes.append(createReadProp(childName, childProp)));
        propTypes.append("}\n\n");

        propTypes.append("input ").append(StringUtils.capitalize(name)).append("Input {\n");
        ((EntityMetadataProp) prop).getProperties().forEach((childName, childProp) ->
            propTypes.append(createWriteProp(childName, childProp)));
        propTypes.append("}\n\n");

        propTypes.append(getPropTypes(((EntityMetadataProp) prop).getProperties(), false));
      }
    });

    return propTypes.toString();
  }

  private static String getReadProps(Map<String, MetadataProp> props) {
    final StringBuilder readPropsBuilder = new StringBuilder();
    props.forEach((name, prop) -> readPropsBuilder.append(createReadProp(name, prop)));
    return readPropsBuilder.toString();
  }

  private static String createReadProp(String name, MetadataProp metadataProp) {
    if (metadataProp instanceof UriMetadataProp) {
      return name + ": Uri @rdf(predicate: \"" + metadataProp.getPredicate() + "\", " +
          "direction: \"OUT\", isList: false, isObject: true, isValue: false)\n";
    }
    if (metadataProp instanceof SimpleMetadataProp) {
      return name + ": Value @rdf(predicate: \"" + metadataProp.getPredicate() + "\", " +
          "direction: \"OUT\", isList: false, isObject: false, isValue: true)\n";
    }
    if (metadataProp instanceof EntityMetadataProp) {
      return name + ": " + StringUtils.capitalize(name) + " @rdf(predicate: \"" + metadataProp.getPredicate() + "\", " +
          "direction: \"OUT\", isList: false, isObject: true, isValue: false)\n";
    }
    return null;
  }

  private static String createWriteProp(String name, MetadataProp metadataProp) {
    if (metadataProp instanceof UriMetadataProp) {
      return name + ": String\n";
    }
    if (metadataProp instanceof SimpleMetadataProp) {
      return name + ": String\n";
    }
    if (metadataProp instanceof EntityMetadataProp) {
      return name + ": " + StringUtils.capitalize(name) + "Input\n";
    }
    return null;
  }
}
