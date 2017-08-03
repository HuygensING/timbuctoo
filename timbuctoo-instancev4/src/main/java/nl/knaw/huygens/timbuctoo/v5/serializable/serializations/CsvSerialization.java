package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serializable;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serialization;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class CsvSerialization implements Serialization {
  private static final Logger LOG = getLogger(CsvSerialization.class);
  public static final String SEPERATOR = ".";

  private final CSVPrinter csvPrinter;

  public CsvSerialization(OutputStream outputStream) throws IOException {
    csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.EXCEL);
  }

  @Override
  public void serialize(Serializable data) throws IOException {
    List<Map<String, Object>> list = findListToUseForRows(data);

    TocItem toc = generateToc(list);

    writeHeader(toc, "");
    csvPrinter.println();

    writeBody(list, toc);

    csvPrinter.flush();
    csvPrinter.close();
  }

  /**
   * Walk the object and pass through each map with only one key until we find a list. That list will become the rows
   * all nested lists will become columns
   *
   * @return the first encountered list, or the top level object wrapped in a list if no, or multiple lists where found
   */
  public List<Map<String, Object>> findListToUseForRows(Serializable data) {
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

  public TocItem generateToc(List<Map<String, Object>> list) throws IOException {
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

  private void writeHeader(TocItem toc, String prefix) throws IOException {
    if (toc.contents.isEmpty()) {
      csvPrinter.print(prefix.substring(0, prefix.length() - SEPERATOR.length()));
    } else {
      for (Map.Entry<String, TocItem> property : toc.contents.entrySet()) {
        writeHeader(property.getValue(), prefix + property.getKey() + SEPERATOR);
      }
    }
  }

  public void writeBody(List<Map<String, Object>> list, TocItem toc) throws IOException {
    for (int i = 0; i < list.size(); i++) {
      final Object item = list.get(i);
      writeBodyRecurser(item, toc);
      csvPrinter.println();
    }
  }

  private void writeBodyRecurser(Object data, TocItem toc) throws IOException {
    if (toc.contents.isEmpty()) {
      if (data instanceof TypedValue) {
        csvPrinter.print(((TypedValue) data).getValue());
      } else {
        csvPrinter.print(data);
      }
    } else {
      if (data  == null) {
        for (Map.Entry<String, TocItem> tocEntry : toc.contents.entrySet()) {
          writeBodyRecurser(null, tocEntry.getValue());
        }
      } else if (data instanceof List) {
        List list = (List) data;
        for (int i = 0; i <= toc.maxCount; i++) {
          if (i < list.size()) {
            final Object o = list.get(i);
            writeBodyRecurser(o, toc.contents.get(i + ""));
          } else {
            writeBodyRecurser(null, toc.contents.get(i + ""));
          }
        }
      } else if (data instanceof Map) {
        Map map = (Map) data;
        for (Map.Entry<String, TocItem> tocEntry : toc.contents.entrySet()) {
          writeBodyRecurser(map.get(tocEntry.getKey()), tocEntry.getValue());
        }
      }
    }
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
