package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNames;

import java.util.Iterator;
import java.util.List;

public class PersonNamesExcelDescription implements ExcelDescription {
  public static final int VALUE_WIDTH = 2;
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
                                           .sorted((sizeA, sizeB) -> sizeA > sizeB ? -1 : 1).iterator();
    return sortedSizes.hasNext() ? sortedSizes.next() : 0;
  }

  @Override
  public int getCols() {
    // Amount of names
    return value.list.size() * VALUE_WIDTH;
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
        result[componentIndex][i * VALUE_WIDTH] = personName.getComponents().get(componentIndex).getType().getName();
        result[componentIndex][i * VALUE_WIDTH + 1] = personName.getComponents().get(componentIndex).getValue();
      }
    }
    return result;
  }

  @Override
  public int getValueWidth() {
    return VALUE_WIDTH;
  }

  @Override
  public List<String> getValueDescriptions() {
    List<String> result = Lists.newArrayList();

    for (int i = 0; i < value.list.size(); i++) {
      result.add(Integer.toString(i + 1));
    }

    return result;
  }
}
