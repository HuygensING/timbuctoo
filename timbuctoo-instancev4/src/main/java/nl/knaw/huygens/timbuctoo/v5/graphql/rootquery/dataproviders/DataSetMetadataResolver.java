package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders;

import com.coxautodev.graphql.tools.GraphQLResolver;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataSetMetadataResolver implements GraphQLResolver<PromotedDataSet> {

  private final DataSetRepository dataSetRepository;

  public DataSetMetadataResolver(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  public ContactInfo getOwner(PromotedDataSet input) {
    return ContactInfo.contactInfo("", ""); //FIXME:
  }

  public ContactInfo getContact(PromotedDataSet input) {
    return ContactInfo.contactInfo("", ""); //FIXME:
  }

  public ProvenanceInfo getProvenanceInfo(PromotedDataSet input) {
    return ProvenanceInfo.provenanceInfo("", ""); //FIXME:
  }

  public License getLicense(PromotedDataSet input) {
    return License.license(""); //FIXME:
  }


  public String getDataSetId(PromotedDataSet input) {
    return input.getCombinedId();
  }

  public String getTitle(PromotedDataSet input) {
    return "FIXME: the title";//FIXME
  }

  public String getDescription(PromotedDataSet input) {
    return "FIXME: the description";//FIXME
  }

  public String getImageUrl(PromotedDataSet input) {
    return "http://lorempixel.com/640/480/cats?" + Math.random();//FIXME
  }

  public CollectionMetadataList getCollections(PromotedDataSet input, int count, String cursor) {
    final DataSet dataSet = dataSetRepository.getDataSet(input.getOwnerId(), input.getDataSetId()).get();

    final TypeNameStore typeNameStore = dataSet.getTypeNameStore();
    final List<ImmutableCollectionMetadata> colls = dataSet
      .getSchemaStore()
      .getTypes().values().stream()
      .map(x -> {
        final long occurrences = x.getOccurrences();
        return ImmutableCollectionMetadata.builder()
          .collectionId(typeNameStore.makeGraphQlname(x.getName()))
          .collectionListId(typeNameStore.makeGraphQlname(x.getName()) + "List")
          .uri(x.getName())
          .total(occurrences)
          .properties(ImmutablePropertyList.builder()
            .prevCursor(Optional.empty())
            .nextCursor(Optional.empty())
            .items(() -> x.getPredicates().stream().map(pred -> {
                return (Property) ImmutableProperty.builder()
                  .density((pred.getOccurrences() * 100) / occurrences)
                  .name(typeNameStore.makeGraphQlnameForPredicate(pred.getName(), pred.getDirection()))
                  .referenceTypes(ImmutableStringList.builder()
                    .prevCursor(Optional.empty())
                    .nextCursor(Optional.empty())
                    .items(() -> pred.getReferenceTypes().stream().map(typeNameStore::makeGraphQlname).iterator())
                    .build()
                  )
                  .valueTypes(ImmutableStringList.builder()
                    .prevCursor(Optional.empty())
                    .nextCursor(Optional.empty())
                    .items(() -> pred.getReferenceTypes().stream().map(typeNameStore::makeGraphQlValuename).iterator())
                    .build()
                  )
                  .build();
              }
            ).iterator())
            .build())
          .build();
      })
      .collect(Collectors.toList());
    return ImmutableCollectionMetadataList.builder()
      .nextCursor(Optional.empty())
      .prevCursor(Optional.empty())
      .items(colls)
      .build();
  }

}
