package nl.knaw.huygens.timbuctoo.v5.serializable;

import java.io.IOException;
import java.util.List;

public class SerializableList implements Serializable {
  private final List<Serializable> data;

  public SerializableList(List<Serializable> data) {
    this.data = data;
  }

  @Override
  public void serialize(Serialization serialization) throws IOException {
    serialization.onStartList();
    int count = 0;
    for (Serializable datum : data) {
      serialization.onListItem(count++);
      datum.serialize(serialization);
    }
    serialization.onCloseList();
  }

  @Override
  public void generateToC(ResultToC siblingEntity) {
    siblingEntity.notifyCount(data.size());
    ResultToC contents = siblingEntity.getContents();
    for (Serializable value : data) {
      value.generateToC(contents);
    }
  }

}
