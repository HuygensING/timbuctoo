require 'json'

require '../lib/mixins/converters/to_year_converter'
require '../lib/timbuctoo_solr/default_mapper'
require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'

class FederatedIndexer

  def initialize(options)
    @solr_io = SolrIO.new(options[:solr_url], :authorization => options[:solr_auth])
    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url])
  end

  def run
    datasets = @timbuctoo_io.fetch_datasets

    archetype_dataset = datasets.select{|dataset| dataset.name.eql?("Admin")}.first
    puts archetype_dataset.inspect

    datasets.reject{|dataset| dataset.name.eql?("Admin") or dataset.name.eql?("Base")}.each do |dataset|

      dataset.metadata.each do |_, collection|
        next if collection[:relationCollection]
        puts "#{collection[:collectionName]} < #{collection[:archetypeName]}"

      end
    end
  end
end