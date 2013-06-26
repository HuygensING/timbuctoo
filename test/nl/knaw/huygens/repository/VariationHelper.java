package nl.knaw.huygens.repository;

import java.util.List;

import com.google.common.collect.Lists;

public class VariationHelper {
  public static List<String> createVariations(String... variations) {
    return Lists.newArrayList(variations);
  }
}
