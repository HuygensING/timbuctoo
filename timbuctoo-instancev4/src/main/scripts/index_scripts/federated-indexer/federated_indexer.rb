require 'json'
require 'pp'
require '../lib/mixins/converters/to_year_converter'
require '../lib/timbuctoo_solr/default_mapper'
require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'

class ArchetypeConfig
  attr_reader :name

  def ArchetypeConfig.conversion_configs
    {
      :text => [{
        :postfix => "_t"
      }],
      :select => [{
        :postfix => "_s"
      }],
      :multiselect => [{
        :postfix => "_ss",
      }],
      :names => [{
        :postfix => "_s",
        :converter_type => "names"
      }],
      :datable => [{
        :postfix => "_i",
        :converter_type => "year"
       }, {
        :postfix => "_s"
      }],
      :links => [{
        :postfix => "_s",
        :converter_type => "links"
      }]
    }
  end

  def initialize(name, properties)
    @name = name
    @properties = properties
  end

  def resolve_type_mapping (prop)
    { :raw_type => prop[:type] }
  end

  def resolve_property_configs
    property_configs = [
      { :name => '@displayName', :converted_name => 'displayName_s' },
      { :name => '@displayName', :converted_name => 'displayName_t' },
      { :name => '_id', :converted_name => 'tim_id_s' },
      { :name => "^rdfUri", :converted_name => 'rdfUri_s' }
    ]
    @properties.reject {|prop| prop[:type].eql?("relation") }.each do |prop|
      ArchetypeConfig.conversion_configs[prop[:type].to_sym].each do |conf|
        property_configs << {
            :name => prop[:name],
            :converted_name => "#{prop[:name]}#{conf[:postfix]}",
            :type => conf.key?(:converter_type) ? conf[:converter_type] : nil
        }
      end
    end

    property_configs
  end

  def get
    {
        :properties => resolve_property_configs,
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

        @timbuctoo_io.scrape_collection(collection[:collectionName], :debug_sample => true)
      end
    end
  end
end