package nl.knaw.huygens.solr;

/*
 * #%L
 * Timbuctoo search
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

  private SolrUtils() {
    throw new AssertionError("Non-instantiable class");
  }

}
