package nl.knaw.huygens.timbuctoo.experimental.exports.excel;

import nl.knaw.huygens.timbuctoo.experimental.exports.ExcelDescription;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.model.PersonNames;

import java.util.Iterator;

import static java.util.stream.Collectors.toList;

public class PersonNamesExcelDescription implements ExcelDescription {
  private final String type;
  private final PersonNames value;

  public PersonNamesExcelDescription(PersonNames personNames, String typeId) {
    this.value = personNames;
    this.type = typeId;
  }

  @Override
  public int getRows() {
    // Max. amount of components
    Iterator<Integer> sortedSizes = value.list.stream().map(personName -> personName.getComponents().size())
                                           .sorted((sizeA, sizeB) -> sizeA < sizeB ? -1 : 1).iterator();
    return sortedSizes.hasNext() ? sortedSizes.next() : 1;
  }

  @Override
  public int getCols() {
    // Amount of names
    return value.list.size() * 2;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String[][] getCells() {
    // -------------------------------------------
    // | FORENAME | A.B.C. | FORENAME  | Albert  |
    // | SURNAME  | Dear   | NAME_LINK | van     |
    // |          |        | SURNAME   | Deer    |
    // -------------------------------------------

    String[][] result = new String[getRows()][getCols()];
    for (int i = 0; i < value.list.size(); i++) {
      PersonName personName = value.list.get(i);
      for (int componentIndex = 0; componentIndex < personName.getComponents().size(); componentIndex++) {
        PersonNameComponent component = personName.getComponents().get(componentIndex);
        result[i][componentIndex * 2] = component.getType().getName();
        result[i][componentIndex * 2 + 1] = component.getValue();
      }
    }
    return result;
  }
}
