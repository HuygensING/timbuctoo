package nl.knaw.huygens.timbuctoo.dropwizard.contenttypes;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * MIME-Type Parser
 *
 * This class provides basic functions for handling mime-types. It can handle
 * matching mime-types against a list of media-ranges. See section 14.1 of the
 * HTTP specification [RFC 2616] for a complete explanation.
 *
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1
 *
 * A port to Java of Joe Gregorio's MIME-Type Parser:
 *
 * http://code.google.com/p/mimeparse/
 *
 * Ported by Tom Zellman <tzellman@gmail.com>.
 */
public class MimeParser {
  /**
   * Parse results container
   */
  protected static class ParseResults {
    String type;

    String subType;

    // !a dictionary of all the parameters for the media range
    Map<String, String> params;

    @Override
    public String toString() {
      StringBuilder stringBuffer = new StringBuilder("('" + type + "', '" + subType + "', {");
      for (String k : params.keySet()) {
        stringBuffer.append("'").append(k).append("':'").append(params.get(k)).append("',");
      }
      return stringBuffer.append("})").toString();
    }
  }

  /**
   * Carves up a mime-type and returns a ParseResults object
   *
   * <p>For example, the media range 'application/xhtml;q=0.5' would get parsed
   * into:
   *
   * <p>('application', 'xhtml', {'q', '0.5'})
   */
  protected static ParseResults parseMimeType(String mimeType) {
    String[] parts = StringUtils.split(mimeType, ";");
    ParseResults results = new ParseResults();
    results.params = new HashMap<>();

    for (int i = 1; i < parts.length; ++i) {
      String part = parts[i];
      String[] subParts = StringUtils.split(part, '=');
      if (subParts.length == 2) {
        results.params.put(subParts[0].trim(), subParts[1].trim());
      }
    }
    String fullType = parts[0].trim();

    // Java URLConnection class sends an Accept header that includes a
    // single "*" - Turn it into a legal wildcard.
    if (fullType.equals("*")) {
      fullType = "*/*";
    }
    String[] types = StringUtils.split(fullType, "/");
    results.type = types[0].trim();
    results.subType = types[1].trim();
    return results;
  }

  /**
   * Carves up a media range and returns a ParseResults.
   *
   * <p>For example, the media range 'application/*;q=0.5' would get parsed into:
   *
   * <p>('application', '*', {'q', '0.5'})
   *
   * <p>In addition this function also guarantees that there is a value for 'q'
   * in the params dictionary, filling it in with a proper default if
   * necessary.
   */
  protected static ParseResults parseMediaRange(String range) {
    ParseResults results = parseMimeType(range);
    String qval = results.params.get("q");
    float asFloat = NumberUtils.toFloat(qval, 1);
    if (StringUtils.isBlank(qval) || asFloat < 0 || asFloat > 1) {
      results.params.put("q", "1");
    }
    return results;
  }

  /**
   * Structure for holding a fitness/quality combo
   */
  protected static class FitnessAndQuality implements
    Comparable<FitnessAndQuality> {
    final int fitness;

    final float quality;

    String mimeType; // optionally used

    public FitnessAndQuality(int fitness, float quality) {
      this.fitness = fitness;
      this.quality = quality;
    }

    public int compareTo(FitnessAndQuality fitnessAndQuality) {
      if (fitness == fitnessAndQuality.fitness) {
        if (quality == fitnessAndQuality.quality) {
          return 0;
        } else {
          return quality < fitnessAndQuality.quality ? -1 : 1;
        }
      } else {
        return fitness < fitnessAndQuality.fitness ? -1 : 1;
      }
    }
  }

  /**
   * Find the best match for a given mimeType against a list of media_ranges
   * that have already been parsed by MimeParse.parseMediaRange(). Returns a
   * tuple of the fitness value and the value of the 'q' quality parameter of
   * the best match, or (-1, 0) if no match was found. Just as for
   * quality_parsed(), 'parsed_ranges' must be a list of parsed media ranges.
   */
  protected static FitnessAndQuality fitnessAndQualityParsed(String mimeType,
                                                             Collection<ParseResults> parsedRanges) {
    int bestFitness = -1;
    float bestFitQ = 0;
    ParseResults target = parseMediaRange(mimeType);

    for (ParseResults range : parsedRanges) {
      if ((
          target.type.equals(range.type) ||
          range.type.equals("*") ||
          target.type.equals("*")
        ) && (
          target.subType.equals(range.subType) ||
          range.subType.equals("*") ||
          target.subType.equals("*")
        )) {
        for (String k : target.params.keySet()) {
          int paramMatches = 0;
          if (!k.equals("q") && range.params.containsKey(k) && target.params.get(k).equals(range.params.get(k))) {
            paramMatches++;
          }
          int fitness = (range.type.equals(target.type)) ? 100 : 0;
          fitness += (range.subType.equals(target.subType)) ? 10 : 0;
          fitness += paramMatches;
          if (fitness > bestFitness) {
            bestFitness = fitness;
            bestFitQ = NumberUtils
              .toFloat(range.params.get("q"), 0);
          }
        }
      }
    }
    return new FitnessAndQuality(bestFitness, bestFitQ);
  }

  /**
   * Find the best match for a given mime-type against a list of ranges that
   * have already been parsed by parseMediaRange(). Returns the 'q' quality
   * parameter of the best match, 0 if no match was found. This function
   * bahaves the same as quality() except that 'parsed_ranges' must be a list
   * of parsed media ranges.
   */
  protected static float qualityParsed(String mimeType,
                                       Collection<ParseResults> parsedRanges) {
    return fitnessAndQualityParsed(mimeType, parsedRanges).quality;
  }

  /**
   * Returns the quality 'q' of a mime-type when compared against the
   * mediaRanges in ranges. For example:
   */
  public static float quality(String mimeType, String ranges) {
    List<ParseResults> results = new LinkedList<>();
    for (String r : StringUtils.split(ranges, ',')) {
      results.add(parseMediaRange(r));
    }
    return qualityParsed(mimeType, results);
  }

  /**
   * Takes a list of supported mime-types and finds the best match for all the
   * media-ranges listed in header. The value of header must be a string that
   * conforms to the format of the HTTP Accept: header. The value of
   * 'supported' is a list of mime-types.
   *
   * <p>MimeParse.bestMatch(Arrays.asList(new String[]{"application/xbel+xml",
   * "text/xml"}), "text/*;q=0.5,*; q=0.1") 'text/xml'
   */
  public static Optional<String> bestMatch(Collection<String> supported, String header) {
    List<ParseResults> parseResults = new LinkedList<>();
    List<FitnessAndQuality> weightedMatches = new LinkedList<>();
    for (String r : StringUtils.split(header, ',')) {
      parseResults.add(parseMediaRange(r));
    }

    for (String s : supported) {
      FitnessAndQuality fitnessAndQuality = fitnessAndQualityParsed(
        s,
        parseResults
      );
      fitnessAndQuality.mimeType = s;
      weightedMatches.add(fitnessAndQuality);
    }
    Collections.sort(weightedMatches);

    FitnessAndQuality lastOne = weightedMatches.getLast();
    return lastOne.quality != 0f ? Optional.of(lastOne.mimeType) : Optional.empty();
  }
}
