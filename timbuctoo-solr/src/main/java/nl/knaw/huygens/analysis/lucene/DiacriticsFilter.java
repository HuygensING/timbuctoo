package nl.knaw.huygens.analysis.lucene;

/*
 * #%L
 * Timbuctoo Solr
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

import com.google.common.collect.Lists;

/**
 * A Lucene filter that combines functionality of a SynonymFilter
 * and an ASCIIFoldingFilter.
 * It passes both the original token and the form with diacritics
 * removed to the next filter in the chain.
 * The implication for users is that if they search for a term
 * with diacritics they get words exactly spelled like that,
 * but if they enter the form without diacritics they get all
 * equivalents with diacritics.
 * Thus the filter should be used when indexing, but not when
 * searching.
 */
public final class DiacriticsFilter extends TokenFilter {

  private final PositionIncrementAttribute posIncrAtt;
  private final CharTermAttribute termAtt;
  private AttributeSource.State current;
  private String simplified;

  public DiacriticsFilter(TokenStream stream) {
    super(stream);
    posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    termAtt = addAttribute(CharTermAttribute.class);
    simplified = null;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (simplified != null) {
      restoreState(current);
      posIncrAtt.setPositionIncrement(0);
      termAtt.setEmpty();
      termAtt.append(simplified);
      simplified = null;
      return true;
    }
    if (!input.incrementToken()) {
      return false;
    }
    String original = new String(termAtt.buffer(), 0, termAtt.length());
    String converted = convert(original);
    if (!converted.equals(original)) {
      simplified = converted;
      current = captureState();
    }
    return true;
  }

  //
  // Implementation note:
  // Using char arrays throughout would be more efficient
  //
  public static String convert(String s) {
    char[] input = s.toCharArray();
    char[] output = new char[2 * input.length];
    int pos = ASCIIFoldingFilter.foldToASCII(input, 0, output, 0, input.length);
    return new String(output, 0, pos);
  }

  public static List<String> convert(List<String> items) {
    List<String> result = Lists.newArrayListWithCapacity(items.size());
    for (String item : items) {
      result.add(convert(item));
    }
    return result;
  }

}
