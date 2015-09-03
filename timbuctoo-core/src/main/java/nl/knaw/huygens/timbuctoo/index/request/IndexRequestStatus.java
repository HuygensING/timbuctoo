package nl.knaw.huygens.timbuctoo.index.request;

public class IndexRequestStatus {
  private IndexRequest.Status status;

  private static final IndexRequestStatus REQUESTED = new IndexRequestStatus(IndexRequest.Status.REQUESTED);
  private static final IndexRequestStatus IN_PROGRESS = new IndexRequestStatus(IndexRequest.Status.IN_PROGRESS);
  private static final IndexRequestStatus DONE = new IndexRequestStatus(IndexRequest.Status.DONE);

  private IndexRequestStatus(IndexRequest.Status status) {
    this.status = status;
  }

  public IndexRequestStatus done() {
    return DONE;
  }

  public IndexRequestStatus inProgress() {
    return IN_PROGRESS;
  }

  public static IndexRequestStatus requested() {
    return REQUESTED;
  }

  public IndexRequest.Status getStatus() {
    return status;
  }
}
