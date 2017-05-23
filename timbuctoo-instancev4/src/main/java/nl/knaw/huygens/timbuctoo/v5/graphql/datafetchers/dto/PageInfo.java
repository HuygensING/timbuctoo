package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

public class PageInfo {
  private final boolean hasNextPage;
  private final boolean hasPreviousPage;
  private final String startCursor;
  private final String endCursor;

  public PageInfo(boolean hasNextPage, String endCursor, boolean hasPreviousPage, String startCursor) {
    this.hasNextPage = hasNextPage;
    this.hasPreviousPage = hasPreviousPage;
    this.startCursor = startCursor;
    this.endCursor = endCursor;
  }

  public boolean isHasNextPage() {
    return hasNextPage;
  }

  public boolean isHasPreviousPage() {
    return hasPreviousPage;
  }

  public String getStartCursor() {
    return startCursor;
  }

  public String getEndCursor() {
    return endCursor;
  }

}
