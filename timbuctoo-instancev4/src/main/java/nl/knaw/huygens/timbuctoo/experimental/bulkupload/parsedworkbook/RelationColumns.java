package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsedworkbook;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.relationtypes.RelationTypeDescription;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class RelationColumns extends ParsedColumns {

  private final String relationTypeName;
  private final String targetType;
  private final Cell captionCell;
  private final int column;

  public RelationColumns(Row headerRow, String relationTypeName, String targetType, int column) {
    this.column = column;
    captionCell = headerRow.getCell(column);
    this.relationTypeName = relationTypeName;
    this.targetType = targetType;
  }

  public boolean isValid(Vre vre, Collection ownCollection, Map<String, RelationTypeDescription> relationDescriptions) {
    RelationTypeDescription description = relationDescriptions.get(relationTypeName);
    Optional<Collection> otherCollectionOpt = vre.getCollectionForCollectionName(targetType);
    if (description == null) {
      markError("Relationtype " + relationTypeName + " is not a known relation");
      return false;
    }
    if (!otherCollectionOpt.isPresent()) {
      markError("Target " + targetType + " is not a collection in this VRE");
      return false;
    }
    Collection otherCollection = otherCollectionOpt.get();

    String source;
    String target;
    if (description.getRegularName().equals(relationTypeName)) {
      source = ownCollection.getAbstractType();
      target = otherCollection.getAbstractType();
    } else {
      source = otherCollection.getAbstractType();
      target = ownCollection.getAbstractType();
    }
    if (!source.equals(description.getSourceTypeName())) {
      markError(source + " is not allowed as the source collection of " + relationTypeName);
      return false;
    }
    if (!target.equals(description.getTargetTypeName())) {
      markError(source + " is not allowed as the target collection of " + relationTypeName);
      return false;
    }
    return true;
  }

  public String getRelationTypeName() {
    return relationTypeName;
  }

  public void applyData(Vertex rowVertex, Row row, BiFunction<String, String, Optional<Vertex>> findTargetVertex,
                        ParsedCollectionRange.EdgeProducer edgeProducer) {
    Cell cell = row.getCell(column);
    try {
      final Optional<String> value = Helpers.getValueAsString(cell);
      if (value.isPresent()) {
        final Optional<Vertex> relatedVertex = findTargetVertex.apply(targetType, value.get());
        if (relatedVertex.isPresent() && edgeProducer.call(rowVertex, relatedVertex.get(), relationTypeName)) {
          Helpers.addSuccess(cell);
        } else {
          Helpers.addFailure(cell, "Referenced row not found");
        }
      }
    } catch (IOException e) {
      Helpers.addFailure(cell, "An error is not a valid reference to another row");
    }
  }

  public void markError(String msg) {
    Helpers.addFailure(captionCell, msg);
  }

  public String getTargetType() {
    return targetType;
  }
}
