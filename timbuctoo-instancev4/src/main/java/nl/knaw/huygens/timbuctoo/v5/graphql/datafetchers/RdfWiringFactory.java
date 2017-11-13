package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.TypeResolutionEnvironment;
import graphql.language.BooleanValue;
import graphql.language.Directive;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.language.StringValue;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.TypeResolver;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.InterfaceWiringEnvironment;
import graphql.schema.idl.UnionWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.CollectionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.QuadStoreLookUpSubjectByUriFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.RelationDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.RelationsOfSubjectDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.TypedLiteralDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.UnionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.valueOf;

public class RdfWiringFactory implements WiringFactory {
  private final UriFetcher uriFetcher;
  private final LookUpSubjectByUriFetcherWrapper lookupFetcher;
  private final ObjectTypeResolver objectTypeResolver;
  private final DataSetRepository dataSetRepository;
  private final PaginationArgumentsHelper argumentsHelper;

  public RdfWiringFactory(DataSetRepository dataSetRepository, PaginationArgumentsHelper argumentsHelper) {
    this.dataSetRepository = dataSetRepository;
    this.argumentsHelper = argumentsHelper;
    objectTypeResolver = new ObjectTypeResolver();
    uriFetcher = new UriFetcher();
    lookupFetcher = new LookUpSubjectByUriFetcherWrapper("uri", new QuadStoreLookUpSubjectByUriFetcher());
  }

  @Override
  public boolean providesTypeResolver(InterfaceWiringEnvironment environment) {
    return true;
  }

  @Override
  public boolean providesTypeResolver(UnionWiringEnvironment environment) {
    return true;
  }

  @Override
  public TypeResolver getTypeResolver(InterfaceWiringEnvironment environment) {
    return objectTypeResolver;
  }

  @Override
  public TypeResolver getTypeResolver(UnionWiringEnvironment environment) {
    return objectTypeResolver;
  }

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    return environment.getFieldDefinition().getDirective("fromCollection") != null ||
      environment.getFieldDefinition().getDirective("rdf") != null ||
      environment.getFieldDefinition().getDirective("uri") != null ||
      environment.getFieldDefinition().getDirective("passThrough") != null ||
      environment.getFieldDefinition().getDirective("related") != null ||
      environment.getFieldDefinition().getDirective("dataSet") != null;
  }

  @Override
  public DataFetcher getDataFetcher(FieldWiringEnvironment environment) {
    if (environment.getFieldDefinition().getDirective("passThrough") != null) {
      return DataFetchingEnvironment::getSource;
    } else if (environment.getFieldDefinition().getDirective("related") != null) {
      final Directive directive = environment.getFieldDefinition().getDirective("related");
      String source = ((StringValue) directive.getArgument("source").getValue()).getValue();
      String predicate = ((StringValue) directive.getArgument("predicate").getValue()).getValue();
      String direction = ((StringValue) directive.getArgument("direction").getValue()).getValue();
      return new CollectionFetcherWrapper(argumentsHelper, new RelationsOfSubjectDataFetcher(
        source,
        predicate,
        Direction.valueOf(direction)
      ));
    } else if (environment.getFieldDefinition().getDirective("fromCollection") != null) {
      final Directive directive = environment.getFieldDefinition().getDirective("fromCollection");
      String uri = ((StringValue) directive.getArgument("uri").getValue()).getValue();
      boolean listAll = ((BooleanValue) directive.getArgument("listAll").getValue()).isValue();
      if (listAll) {
        return new CollectionFetcherWrapper(argumentsHelper, new CollectionDataFetcher(uri));
      } else {
        return lookupFetcher;
      }
    } else if (environment.getFieldDefinition().getDirective("rdf") != null) {
      final Directive directive = environment.getFieldDefinition().getDirective("rdf");
      String uri = ((StringValue) directive.getArgument("predicate").getValue()).getValue();
      Direction direction = valueOf(((StringValue) directive.getArgument("direction").getValue()).getValue());
      boolean isList = ((BooleanValue) directive.getArgument("isList").getValue()).isValue();
      boolean isObject = ((BooleanValue) directive.getArgument("isObject").getValue()).isValue();
      boolean isValue = ((BooleanValue) directive.getArgument("isValue").getValue()).isValue();
      if (isObject && isValue) {
        return new DataFetcherWrapper(argumentsHelper, isList, new UnionDataFetcher(uri, direction));
      } else {
        if (isObject) {
          return new DataFetcherWrapper(argumentsHelper, isList, new RelationDataFetcher(uri, direction));
        } else {
          return new DataFetcherWrapper(argumentsHelper, isList, new TypedLiteralDataFetcher(uri));
        }
      }
    } else if (environment.getFieldDefinition().getDirective("uri") != null) {
      return uriFetcher;
    } else if (environment.getFieldDefinition().getDirective("dataSet") != null) {
      final Directive directive = environment.getFieldDefinition().getDirective("dataSet");
      String userId = ((StringValue) directive.getArgument("userId").getValue()).getValue();
      String dataSetId = ((StringValue) directive.getArgument("dataSetId").getValue()).getValue();
      final DataSet dataSet = dataSetRepository.unsafeGetDataSetWithoutCheckingPermissions(userId, dataSetId)
        .orElse(null);
      return dataFetchingEnvironment -> new DatabaseResult() {
        @Override
        public DataSet getDataSet() {
          return dataSet;
        }
      };
    }
    return null;
  }

  @Override
  public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
    return new PropertyDataFetcher(environment.getFieldDefinition().getName());
  }

  private static class ObjectTypeResolver implements TypeResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectTypeResolver.class);

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment environment) {

      String typeName = null;
      if (environment.getObject() instanceof TypedValue) {
        final TypedValue typedValue = (TypedValue) environment.getObject();
        final String typeUri = typedValue.getType();
        final String prefix = typedValue.getDataSet().getMetadata().getCombinedId();
        typeName =
          prefix +
          "_" +
          typedValue.getDataSet().getTypeNameStore().makeGraphQlValuename(typeUri);
      } else {
        //Often a thing has one type. In that case this lambda is easy to implement. Simply return that type
        //In rdf things can have more then one type though (types are like java interfaces)
        //Since this lambda only allows us to return 1 type we need to do a bit more work and return one of the types
        // that
        //the user actually requested
        final SubjectReference subjectReference = (SubjectReference) environment.getObject();
        final String prefix = subjectReference.getDataSet().getMetadata().getCombinedId();
        Set<String> typeUris = subjectReference.getTypes();
        final TypeNameStore typeNameStore = subjectReference.getDataSet().getTypeNameStore();
        if (typeUris.isEmpty()) {
          typeName = prefix + "_" + typeNameStore.makeGraphQlname(RdfConstants.UNKNOWN);
        } else {
          for (Selection selection : environment.getField().getSelectionSet().getSelections()) {
            if (selection instanceof InlineFragment) {
              InlineFragment fragment = (InlineFragment) selection;
              String typeUri = typeNameStore.makeUri(
                fragment.getTypeCondition().getName().substring(prefix.length() + 1)
              );
              if (typeUris.contains(typeUri)) {
                typeName = prefix + "_" + typeNameStore.makeGraphQlname(typeUri);
                break;
              }
            } else {
              LOG.error("I have a union type whose selection is not an InlineFragment!");
            }
          }
        }
      }
      if (typeName == null) {
        return null;
      } else {
        final GraphQLObjectType type = (GraphQLObjectType) environment.getSchema().getType(typeName);
        return type;
      }
    }
  }
}
