package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;


class PromisedTriplesMapBuilder {

  private String requesterUri;
  private TriplesMapBuilder triplesMapBuilder;

  PromisedTriplesMapBuilder(String requesterUri, TriplesMapBuilder triplesMapBuilder) {

    this.requesterUri = requesterUri;
    this.triplesMapBuilder = triplesMapBuilder;
  }

  public String getRequesterUri() {
    return requesterUri;
  }

  public TriplesMapBuilder getTriplesMapBuilder() {
    return triplesMapBuilder;
  }

  @Override
  public String toString() {
    return "PromisedTriplesMapBuilder{" +
            "requesterUri='" + requesterUri + '\'' +
            ", triplesMapBuilder=" + triplesMapBuilder.getUri() +
            '}';
  }
}
