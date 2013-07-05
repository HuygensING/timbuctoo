package nl.knaw.huygens.solr;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.parser.SolrQueryParserBase;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class SolrUtils {
  // Special Lucene characters: + - & | ! ( ) { } [ ] ^ " ~ * ? : \
  private static final Pattern SPECIAL = Pattern.compile("[+\\-&|!(){}\\[\\]\\^\"~*?:\\\\]");

  public static String fuzzy(String text) {
    if (StringUtils.isBlank(text) || SPECIAL.matcher(text).find()) {
      return text;
    }
    StringBuilder builder = new StringBuilder();
    String[] terms = StringUtils.split(text);
    appendTerm(builder, terms[0]);
    for (int i = 1; i < terms.length; i++) {
      builder.append(" AND ");
      appendTerm(builder, terms[i]);
    }
    return builder.toString();
  }

  private static void appendTerm(StringBuilder builder, String term) {
    builder.append(term);
    if (term.length() < 4) {
      builder.append("~0.5");
    } else {
      builder.append("~0.7");
    }
  }

  public static String escapeFacetId(String string) {
    return SolrQueryParserBase.escape(string).replaceAll(" ", "\\\\ ");
  }

  public static List<String> splitTerms(String terms) {
    Iterable<String> split = Splitter.on(" ").split(terms);
    StringBuilder tmpTerm = null;
    boolean append = false;
    List<String> termlist = Lists.newArrayList();
    for (String part : split) {
      if (part.startsWith("\"")) {
        tmpTerm = new StringBuilder(part);
        append = true;

      } else if (part.endsWith("\"")) {
        tmpTerm.append(" ").append(part);
        termlist.add(tmpTerm.toString());
        append = false;

      } else if (append) {
        tmpTerm.append(" ").append(part);

      } else {
        termlist.add(part);
      }
    }
    return termlist;
  }

  private SolrUtils() {}
}
