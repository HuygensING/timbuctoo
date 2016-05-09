package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class RelationColumns extends Columns {

  private final String name;
  private final String targetType;
  private final List<Cell> items = new ArrayList<>();
  private final Cell captionCell;

  public RelationColumns(int minRow, int maxRow, int headerRow, Sheet sheet, String name, String targetType,
                         int column) {
    captionCell = sheet.getRow(headerRow).getCell(column);
    this.name = name; //fixme name -> relationName
    this.targetType = targetType;
    for (int r = minRow; r <= maxRow; r++) {
      Cell propVal = sheet.getRow(r).getCell(column);
      items.add(propVal);
    }
  }

  public boolean isValid(Vre vre, Collection ownCollection, Map<String, RelationDescription> relationDescriptions) {
    RelationDescription description = relationDescriptions.get(name);
    Optional<Collection> otherCollectionOpt = vre.getCollectionForCollectionName(targetType);
    if (description == null) {
      markError("Relationtype " + name + " is not a known relation");
      return false;
    }
    if (!otherCollectionOpt.isPresent()) {
      markError("Target " + targetType + " is not a collection in this VRE");
      return false;
    }
    Collection otherCollection = otherCollectionOpt.get();

    String source;
    String target;
    if (description.getRegularName().equals(name)) {
      source = ownCollection.getAbstractType();
      target = otherCollection.getAbstractType();
    } else {
      source = otherCollection.getAbstractType();
      target = ownCollection.getAbstractType();
    }
    if (!source.equals(description.getSourceTypeName())) {
      markError(source + " is not allowed as the source collection of " + name);
      return false;
    }
    if (!target.equals(description.getTargetTypeName())) {
      markError(source + " is not allowed as the target collection of " + name);
      return false;
    }
    return true;
  }

  public String getName() {
    return name;
  }

  public void applyData(Vertex rowVertex, int index, BiFunction<String, String, Optional<Vertex>> findTargetVertex,
                        CollectionRange.EdgeProducer edgeProducer) {
    Cell cell = items.get(index);
    final Optional<String> value;
    try {
      value = Helpers.getValueAsString(cell);
      if (value.isPresent()) {
        final Optional<Vertex> relatedVertex = findTargetVertex.apply(targetType, value.get());
        if (relatedVertex.isPresent() && edgeProducer.call(rowVertex, relatedVertex.get(), name)) {
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
