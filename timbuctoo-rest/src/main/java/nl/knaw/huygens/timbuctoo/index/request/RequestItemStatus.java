package nl.knaw.huygens.timbuctoo.index.request;

import com.google.common.collect.Lists;

import java.util.List;

class RequestItemStatus {
  private List<String> toDo;
  private List<String> done;

  RequestItemStatus() {
    done = Lists.newArrayList();
    toDo = Lists.newArrayList();
  }

  public void setToDo(List<String> toDo) {
    this.toDo.addAll(toDo);
  }

  public void done(String id) {
    if (toDo.remove(id)) {
      done.add(id);
    }
  }

  public List<String> getToDo() {
    return Lists.newArrayList(toDo);
  }

  public List<String> getDone() {
    return Lists.newArrayList(done);
  }
}
