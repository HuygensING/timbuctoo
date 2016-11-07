require 'json'
require 'pp'
require '../lib/mixins/converters/to_year_converter'
require '../lib/mixins/converters/to_names_converter'
require '../lib/timbuctoo_solr/default_mapper'
require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'

require './archetype_config.rb'
require './federated_mapper.rb'


class FederatedIndexer

  def initialize(options)
    @solr_io = SolrIO.new(options[:solr_url], :authorization => options[:solr_auth])
    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url])
  end

  def run
    datasets = @timbuctoo_io.fetch_datasets

    archetype_configs = datasets.select{|dataset| dataset.name.eql?("Admin")}.first
      .metadata.map{|_, collection| ArchetypeConfig.new(collection[:archetypeName], collection[:properties])}

    forks = []
    datasets.reject{|dataset| dataset.name.eql?("Admin") or dataset.name.eql?("Base")}.each do |dataset|

      dataset.metadata.each do |_, collection|
        next if collection[:relationCollection]


        archetype_config = archetype_configs.select{|conf| conf.name.eql?(collection[:archetypeName])}.first
        mapper = FederatedMapper.new(collection[:collectionName], collection[:archetypeName], dataset.name, archetype_config.get)
        puts "#{collection[:collectionName]} < #{collection[:archetypeName]}"

        forks << Process.fork do
          @timbuctoo_io.scrape_collection(collection[:collectionName],
                                          :with_relations => true,
                                          :process_record => -> (record) { process_record(mapper, record) },
                                          :debug_sample => true)
        end
      end
    end

    forks.each do |pid|
      Process.wait(pid)
    end
  end

  private
  def process_record(mapper, record)
    pp mapper.convert(record)
  end
end