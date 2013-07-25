package nl.knaw.huygens.repository.util;

import org.apache.commons.lang.StringUtils;

/**
 * Fixes latin-1 data that was wrongly converted to UTF-8.
 * 
 * It assumes that the data may also contain parts that were correctly
 * converted. This may typically happen when the data consists of the
 * content of a form.
 */
public class EncodingFixer {

  private static final String[] SOURCE_LOWER = { "Ã¡", "Ã©", "Ã*", "Ã³", "Ãº", "Ã±", "Ã§" };
  private static final String[] TARGET_LOWER = { "á", "é", "í", "ó", "ú", "ñ", "ç" };

  // TODO handle "Á" and "Í"
  private static final String[] SOURCE_UPPER = { "Ã‰", "Ã“", "Ãš", "Ã‘", "Ã‡" };
  private static final String[] TARGET_UPPER = { "É", "Ó", "Ú", "Ñ", "Ç" };

  public static String convert1(String text) {
    text = StringUtils.replaceEach(text, SOURCE_LOWER, TARGET_LOWER);
    return StringUtils.replaceEach(text, SOURCE_UPPER, TARGET_UPPER);
  }

  public static String convert2(String text) {
    String conv1 = convert1(text);
    if (!conv1.equals(text)) {
      String conv2 = convert1(conv1);
      if (!conv2.equals(conv1)) {
        throw new RuntimeException("Instable conversion");
      }
    }
    return conv1;
  }

}
