package nl.knaw.huygens.concordion.extensions;

import nu.xom.Element;
import nu.xom.Elements;
import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ReplaceEmbeddedStylesheetExtension implements ConcordionExtension {
  private final String styleToRemove;
  private final String styleToReplace;

  public ReplaceEmbeddedStylesheetExtension() {
    styleToRemove = readResourceAsString();
    styleToReplace = null;
  }

  public ReplaceEmbeddedStylesheetExtension(String styleToReplace) {
    this.styleToReplace = styleToReplace;
    styleToRemove = readResourceAsString();
  }

  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    if (styleToReplace != null) {
      concordionExtender.withLinkedCSS(styleToReplace, new Resource("/concordion.css"));
    }
    concordionExtender.withDocumentParsingListener(document -> {
      Element html = document.getRootElement();
      Element head = html.getFirstChildElement("head");
      if (head != null) {
        Elements styles = head.getChildElements("style");
        for (int i = 0; i < styles.size(); i++) {
          Element style = styles.get(i);
          if (styleToRemove.equals(style.getValue())) {
            head.removeChild(style);
          }
        }
      }
    });
  }

  private String readResourceAsString() {
    try {
      ClassLoader classLoader = ReplaceEmbeddedStylesheetExtension.class.getClassLoader();
      InputStream in =  classLoader.getResourceAsStream("org/concordion/internal/resource/embedded.css");
      if (in == null) {
        throw new IOException("Resource not found");
      }
      try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader bufferedReader = new BufferedReader(reader);
        while ((line = bufferedReader.readLine()) != null) {
          sb.append(line).append("\n");
        }
        return sb.toString();
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read resource '" + "/org/concordion/internal/resource/embedded.css" + "'", e);
    }
  }
}
