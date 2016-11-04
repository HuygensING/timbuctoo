require 'json'
require 'pp'
require '../lib/mixins/converters/to_year_converter'
require '../lib/timbuctoo_solr/default_mapper'
require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'

=begin
class WwDocumentConfig
  def WwDocumentConfig.get
    {
        :properties => [
            { :name => '_id',  :converted_name => 'id'},
            { :name => 'title', :converted_name => 'displayName_s'},
            { :name => 'title', :converted_name => 'title_t'},
            { :name => 'documentType', :converted_name => 'documentType_s'},
            { :name => 'englishTitle', :converted_name  => '^englishTitle'},
            { :name => 'date', :converted_name => 'date_i', :type => 'year'},
            { :name => 'notes', :converted_name => 'notes_t'},
            { :name => ['^modified', 'timeStamp'], :converted_name => 'modified_l' },
            { :name => ['^modified', 'userId'], :converted_name => 'modifiedBy_s' },
            { :name => ['^modified', 'username'], :converted_name => 'modifiedBy_s' },
        ], :relations => [
        { :relation_name => 'hasPublishLocation', :property_name => 'displayName', :converted_name => 'publishLocation_ss'},
        { :relation_name => 'hasWorkLanguage', :property_name => 'displayName', :converted_name => 'language_ss'},
        { :relation_name => 'hasGenre', :property_name => 'displayName', :converted_name => 'genre_ss'},
        { :relation_name => 'hasDocumentSource', :property_name => 'displayName', :converted_name => 'source_ss'},

        # Locates creators
        { :relation_name => 'isCreatedBy', :property_name => 'id', :converted_name => '@authorIds'}
    ]
    }
  end
end

{:properties=>[{:raw_type=>"text"}],
{:properties=>[{:raw_type=>"text"}, {:raw_type=>"text"}],
   {:raw_type=>"datable"},
   {:raw_type=>"links"}],
   {:raw_type=>"multiselect"},
  [{:raw_type=>"names"},
   {:raw_type=>"select"},
   {:raw_type=>"text"},
  [{:raw_type=>"text"},
  [{:raw_type=>"text"}, {:raw_type=>"select"}, {:raw_type=>"links"}],

=end

class ArchetypeConfig
  attr_reader :name

  ArchetypeConfig.postfixes = {
      :text => "_s",
      :datable => "_i",

  }

  def initialize(name, properties)
    @name = name
    @properties = properties
  end

  def resolve_type_mapping (prop)
    { :raw_type => prop[:type]}
  end

  def get
    {
        :properties => @properties
           .reject {|prop| prop[:type].eql?("relation") }
           .map {|prop| resolve_type_mapping(prop)
           } <<
            { :name => '@displayName', :converted_name => 'displayName_s' }  <<
            { :name => '@displayName', :converted_name => 'displayName_t' } <<
            { :name => '_id', :converted_name => 'tim_id_s' } <<
            { :name => "^rdfUri", :converted_name => 'rdfUri_s' },
        :relations => @properties
          .select {|prop| prop[:type].eql?("relation") }
          .map {|rel| {
            :relation_name => rel[:name],
            :property_name => 'displayName',
            :converted_name => "#{rel[:name]}_ss"
          }
        }
    }
  end
end

class FederatedIndexer

  def initialize(options)
    @solr_io = SolrIO.new(options[:solr_url], :authorization => options[:solr_auth])
    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url])
  end

  def run
    datasets = @timbuctoo_io.fetch_datasets

    archetype_configs = datasets.select{|dataset| dataset.name.eql?("Admin")}.first
      .metadata.map{|_, collection| ArchetypeConfig.new(collection[:archetypeName], collection[:properties])}


    datasets.reject{|dataset| dataset.name.eql?("Admin") or dataset.name.eql?("Base")}.each do |dataset|

      dataset.metadata.each do |_, collection|
        next if collection[:relationCollection]


        archetype_config = archetype_configs.select{|conf| conf.name.eql?(collection[:archetypeName])}.first.get

        puts "#{collection[:collectionName]} < #{collection[:archetypeName]}"
        pp archetype_config

#        @timbuctoo_io.scrape_collection(collection[:collectionName], :debug_sample => true)
      end
    end
  end
end