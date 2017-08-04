package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serializable;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serialization;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.CsvSerialization;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class FlatTableSerialization implements Serialization {
  public static final String SEPARATOR = ".";
  private static final Logger LOG = getLogger(CsvSerialization.class);

  @Override
  public void serialize(Serializable data) throws IOException {
    List<Map<String, Object>> list = findListToUseForRows(data);

    TocItem toc = generateToc(list);

    initialize(getHeader(toc, ""));
    writeBody(list, toc);
    finish();
  }

  /**
   * is called once at the start of the serialization
   */
  protected abstract void initialize(List<String> columnHeaders) throws IOException;

  /**
   * is called once for each row. NOTE! the list may have gaps (null values)
   */
  protected abstract void writeRow(List<TypedValue> values) throws IOException;

  /**
   * is called once at the end of the serialization
   */
  protected abstract void finish() throws IOException;

  /**
   * Walk the object and pass through each map with only one key until we find a list. That list will become the rows
   * all nested lists will become columns
   *
   * @return the first encountered list, or the top level object wrapped in a list if no, or multiple lists where found
   */
  private List<Map<String, Object>> findListToUseForRows(Serializable data) {
    List<Map<String, Object>> list = findListRecurser(data.getData());
    //If no list can be found, we convert the top level object into a list
    if (list == null) {
      list = new ArrayList<>();
      list.add(data.getData());
    }
    return list;
  }

  private List<Map<String, Object>> findListRecurser(Map<String, Object> data) {
    if (data.keySet().isEmpty()) {
      LOG.error("I though maps could not be empty because graphql requires you to ask for at least 1 key");
    }
    if (data.keySet().size() == 1) {
      Object entry = data.values().iterator().next();
      if (entry instanceof Map) {
        return findListRecurser((Map) entry);
      } else if (entry instanceof List) {
        return (List) entry;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  private TocItem generateToc(List<Map<String, Object>> list) throws IOException {
    TocItem toc = new TocItem();
    for (int i = 0; i < list.size(); i++) {
      final Object item = list.get(i);
      generateTocRecurser(item, toc);
    }
    return toc;
  }

  private void generateTocRecurser(Object data, TocItem tocItem) throws IOException {
    if (data instanceof Map) {
      Map<String, Object> value = (Map<String, Object>) data;
      for (Map.Entry<String, Object> entry : value.entrySet()) {
        if (!"@id".equals(entry.getKey()) && !"@type".equals(entry.getKey())) {
          TocItem subItem = tocItem.add(entry.getKey());
          generateTocRecurser(entry.getValue(), subItem);
        }
      }
    } else if (data instanceof List) {
      List list = (List) data;
      for (int i = 0; i < list.size(); i++) {
        final Object item = list.get(i);
        TocItem subItem = tocItem.add(i);
        generateTocRecurser(item, subItem);
      }
    }
  }

  private List<String> getHeader(TocItem toc, String prefix) throws IOException {
    List<String> header = new ArrayList<>();
    if (toc.contents.isEmpty()) {
      header.add(prefix.substring(0, prefix.length() - SEPARATOR.length()));
    } else {
      for (Map.Entry<String, TocItem> property : toc.contents.entrySet()) {
        header.addAll(getHeader(property.getValue(), prefix + property.getKey() + SEPARATOR));
      }
    }
    return header;
  }

  private void writeBody(List<Map<String, Object>> list, TocItem toc) throws IOException {
    for (Map<String, Object> item : list) {
      writeRow(writeBodyRecurser(item, toc));
    }
  }

  private List<TypedValue> writeBodyRecurser(Object data, TocItem toc) throws IOException {
    List<TypedValue> result = new ArrayList<>();
    if (toc.contents.isEmpty()) {
      if (data instanceof TypedValue) {
        result.add((TypedValue) data);
      } else {
        result.add(TypedValue.createFromNative(data));
      }
    } else {
      if (data  == null) {
        for (Map.Entry<String, TocItem> tocEntry : toc.contents.entrySet()) {
          result.addAll(writeBodyRecurser(null, tocEntry.getValue()));
        }
      } else if (data instanceof List) {
        List list = (List) data;
        for (int i = 0; i <= toc.maxCount; i++) {
          if (i < list.size()) {
            final Object o = list.get(i);
            result.addAll(writeBodyRecurser(o, toc.contents.get(i + "")));
          } else {
            result.addAll(writeBodyRecurser(null, toc.contents.get(i + "")));
          }
        }
      } else if (data instanceof Map) {
        Map map = (Map) data;
        for (Map.Entry<String, TocItem> tocEntry : toc.contents.entrySet()) {
          result.addAll(writeBodyRecurser(map.get(tocEntry.getKey()), tocEntry.getValue()));
        }
      }
    }
    return result;
  }

  class TocItem {
    public final Map<String, TocItem> contents = new LinkedHashMap<>();
    public int maxCount = 0;

    public TocItem add(int key) {
      if (maxCount < key) {
        maxCount = key;
      }
      return contents.computeIfAbsent(key + "", x -> new TocItem());
    }

    public TocItem add(String key) {
      return contents.computeIfAbsent(key, x -> new TocItem());
    }
  }
}
