package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CursorSubject;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import org.junit.Test;

import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.PaginationHelper
  .getPaginatedList;
import static nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList.encode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class PaginationHelperTest {

  private static final Base64.Decoder DECODER = Base64.getDecoder();


  public static final Function<CursorSubject, SubjectReference> MAKE_ITEM = c -> new SubjectReference() {
    @Override
    public String getSubjectUri() {
      return c.getSubjectUri();
    }

    @Override
    public Set<String> getTypes() {
      return new HashSet<>();
    }

    @Override
    public DataSet getDataSet() {
      return null;
    }
  };

  @Test
  public void firstPageHasNullAsPrev() throws Exception {
    final PaginatedList<SubjectReference> paginatedList = getPaginatedList(
      Stream.of(
        CursorSubject.create("c1", "http://example.org/1"),
        CursorSubject.create("c2", "http://example.org/2"),
        CursorSubject.create("c3", "http://example.org/3"),
        CursorSubject.create("c4", "http://example.org/4")
      ),
      MAKE_ITEM,
      PaginationArguments.create(2, "", Optional.empty())
    );

    assertThat(decode(paginatedList.getNextCursor()), is("A\nc2"));
    assertThat(paginatedList.getPrevCursor(), is(Optional.empty()));
    assertThat(paginatedList.getItems().size(), is(2));
    assertThat(paginatedList.getItems().get(0).getSubjectUri(), is("http://example.org/1"));
    assertThat(paginatedList.getItems().get(1).getSubjectUri(), is("http://example.org/2"));
  }

  @Test
  public void secondPageHasPrevAndNext() throws Exception {
    final PaginatedList<SubjectReference> paginatedList = getPaginatedList(
      Stream.of(
        CursorSubject.create("c3", "http://example.org/3"),
        CursorSubject.create("c4", "http://example.org/4"),
        CursorSubject.create("c5", "http://example.org/4")
      ),
      MAKE_ITEM,
      PaginationArguments.create(2, encode("A\nc2"), Optional.empty())
    );

    assertThat(decode(paginatedList.getNextCursor()), is("A\nc4"));
    assertThat(decode(paginatedList.getPrevCursor()), is("D\nc3"));
    assertThat(paginatedList.getItems().size(), is(2));
    assertThat(paginatedList.getItems().get(0).getSubjectUri(), is("http://example.org/3"));
    assertThat(paginatedList.getItems().get(1).getSubjectUri(), is("http://example.org/4"));
  }

  @Test
  public void lastPageHasNullAsNext() throws Exception {
    final PaginatedList<SubjectReference> paginatedList = getPaginatedList(
      Stream.of(
        CursorSubject.create("c4", "http://example.org/4"),
        CursorSubject.create("c3", "http://example.org/3"),
        CursorSubject.create("c2", "http://example.org/2"),
        CursorSubject.create("c1", "http://example.org/1")
      ),
      MAKE_ITEM,
      PaginationArguments.create(2, encode("LAST"), Optional.empty())
    );

    assertThat(paginatedList.getNextCursor(), is(Optional.empty()));
    assertThat(decode(paginatedList.getPrevCursor()), is("D\nc3"));
    assertThat(paginatedList.getItems().size(), is(2));

    //still list the items in ascending order
    assertThat(paginatedList.getItems().get(0).getSubjectUri(), is("http://example.org/3"));
    assertThat(paginatedList.getItems().get(1).getSubjectUri(), is("http://example.org/4"));
  }

  @Test
  public void downwardsToFirstPageIsSameAsStartingFromFirstPage() throws Exception {
    final PaginatedList<SubjectReference> paginatedList = getPaginatedList(
      Stream.of(
        CursorSubject.create("c2", "http://example.org/2"),
        CursorSubject.create("c1", "http://example.org/1")
      ),
      MAKE_ITEM,
      PaginationArguments.create(2, encode("D\nc3"), Optional.empty())
    );

    assertThat(decode(paginatedList.getNextCursor()), is("A\nc2"));
    assertThat(decode(paginatedList.getPrevCursor()), is(nullValue()));
    assertThat(paginatedList.getItems().size(), is(2));
    assertThat(paginatedList.getItems().get(0).getSubjectUri(), is("http://example.org/1"));
    assertThat(paginatedList.getItems().get(1).getSubjectUri(), is("http://example.org/2"));
  }


  private String decode(Optional<String> encoded) {
    return encoded.map(s -> new String(DECODER.decode(s.getBytes()))).orElse(null);
  }
}
