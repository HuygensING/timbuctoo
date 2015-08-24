package nl.knaw.huygens.timbuctoo.index.request;

import java.util.List;

public class RequestItemStatus {
  private List<String> toDo;

  public void setToDo(List<String> toDo) {
    this.toDo = toDo;
  }

  public void done(String id) {
  }

  public List<String> getToDo() {
    return toDo;
  }
}
