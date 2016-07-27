package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RrLogicalSource;

public class ReferenceGetter {
  public final RrLogicalSource source;
  public final String targetFieldName;

  public final String referenceJoinFieldName;
  public final String child;
  //Using this you can either generate a joining query and regenerate the URI's or lookup the URI's that you saved
  // during willBeJoinedOn
  //public final Function<Map<String, Object>, String> generateUri;

  public ReferenceGetter(RrLogicalSource source, String targetFieldName, String child, String referenceJoinFieldName) {
    this.source = source;
    this.targetFieldName = targetFieldName;
    this.referenceJoinFieldName = referenceJoinFieldName;
    this.child = child;
  }
}
