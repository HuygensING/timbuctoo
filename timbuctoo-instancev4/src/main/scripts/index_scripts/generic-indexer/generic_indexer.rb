require '../lib/mixins/converters/to_year_converter'
require '../lib/mixins/converters/to_names_converter'
require '../lib/timbuctoo_solr/default_mapper'
require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'
require './generic_configs'

class GenericMapper < DefaultMapper
  include ToYearConverter
  include ToNamesConverter

  def convert_value(input_value, type)
    begin
      return super(input_value, type)
    rescue Exception => e
      puts "convert error: #{e.inspect}"
      return nil
    end
  end
end

class GenericIndexer

  def initialize(options)
    @mappers = {}
    @solr_io = SolrIO.new(options[:solr_url], :authorization => options[:solr_auth])
    GenericConfigs.new(:vre_id => options[:vre_id], :timbuctoo_url => options[:timbuctoo_url]).fetch.each do |config|
      @mappers[config[:collection]] = GenericMapper.new(:properties => config[:properties], :relations => config[:relations])
    end

    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url])
  end

  def run
    @mappers.each do |collection, mapper|
      begin
        @solr_io.create(collection)
      rescue Exception => e
        puts "Index #{collection} already exists"
      end
      @solr_io.delete_data(collection)

      @timbuctoo_io.scrape_collection(collection, :with_relations => true, :process_record => -> (record) {
        convert(mapper, record, collection)
      })

      @solr_io.commit(collection)
    end
  end

  def convert(mapper, record, collection)
    @solr_io.update(collection, [mapper.convert(record)])
  end
end