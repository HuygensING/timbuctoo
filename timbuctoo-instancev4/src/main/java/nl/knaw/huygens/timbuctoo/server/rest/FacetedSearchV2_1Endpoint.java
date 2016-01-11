package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.collect.Lists;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v2.1/search")
@Produces(APPLICATION_JSON)
public class FacetedSearchV2_1Endpoint {

  @POST
  @Path("wwpersons")
  public Response post(SearchRequestV2_1 searchRequest) {
    UUID uuid = UUID.randomUUID();
    URI uri = UriBuilder.fromResource(FacetedSearchV2_1Endpoint.class).path("{id}").build(new Object[]{uuid});
    return Response.created(uri).build();
  }

  @GET
  @Path("{id: [a-f0-9\\-]+}")
  public Response get() {
    SearchResponseV2_1 searchResponse = new SearchResponseV2_1();
    searchResponse.setSortableFields(new WWPersonsFacetedSearchDescription().getSortableFields());
    return Response.ok(searchResponse).build();
  }

  static class SearchRequestV2_1 {
  }

  static class SearchResponseV2_1 {
    private List<Facet> facets;
    private List<String> fullTextSearchFields;
    private List<Ref> refs;
    private List<String> sortableFields;
    private int start;
    private int rows;
    private int numFound;

    public SearchResponseV2_1() {
      facets = Lists.newArrayList();
      fullTextSearchFields = Lists.newArrayList();
      refs = Lists.newArrayList();
      sortableFields = Lists.newArrayList();
    }

    public List<Facet> getFacets() {
      return facets;
    }

    public List<String> getFullTextSearchFields() {
      return fullTextSearchFields;
    }

    public List<Ref> getRefs() {
      return refs;
    }

    public List<String> getSortableFields() {
      return sortableFields;
    }

    public void setSortableFields(List<String> sortableFields){
      this.sortableFields = sortableFields;
    }

    public int getStart() {
      return start;
    }

    public int getRows() {
      return rows;
    }

    public int getNumFound() {
      return numFound;
    }

    private static class Facet {
    }

    private static class Ref {
    }
  }

  private static class WWPersonsFacetedSearchDescription {
    private static final List<String> SORTABLE_FIELDS = Lists.newArrayList(
      "dynamic_k_modified",
      "dynamic_k_birthDate",
      "dynamic_sort_name",
      "dynamic_k_deathDate");

    public List<String> getSortableFields() {
      return SORTABLE_FIELDS;
    }
  }
}
