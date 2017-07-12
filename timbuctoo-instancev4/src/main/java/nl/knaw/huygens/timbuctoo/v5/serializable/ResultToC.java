package nl.knaw.huygens.timbuctoo.v5.serializable;

import java.util.LinkedHashMap;

public class ResultToC {
  private final LinkedHashMap<String, ResultToC> fields = new LinkedHashMap<>();
  private ResultToC contents;
  private int maxCount = -1;

  public LinkedHashMap<String, ResultToC> getFields() {
    return fields;
  }

  public int getMaxCount() {
    return maxCount;
  }

  void notifyCount(int otherCount) {
    maxCount = maxCount < otherCount ? otherCount : maxCount;
  }

  ResultToC getField(String key) {
    return fields.computeIfAbsent(key, k -> new ResultToC());
  }

  void notifyValueField(String key) {
    fields.computeIfAbsent(key, k -> null);
  }

  ResultToC getContents() {
    if (contents == null) {
      contents = new ResultToC();
    }
    return contents;
  }

}
