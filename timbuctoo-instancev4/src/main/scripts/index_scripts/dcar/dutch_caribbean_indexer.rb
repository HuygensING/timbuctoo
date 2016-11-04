require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'
require '../lib/timbuctoo_solr/default_mapper'

require './configs/dcar_archive_config'
require './configs/dcar_archiver_config'
require './configs/dcar_legislation_config'
require './dcar_mapper'

class DutchCaribbeanIndexer
  def initialize(options)
    @options = options

    @mappers = {
      :dcararchives => DcarMapper.new(DcarArchiveConfig.get),
      :dcarlegislations => DcarMapper.new(DcarLegislationConfig.get),
      :dcararchivers => DcarMapper.new(DcarArchiverConfig.get)
    }

    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url], {
        :dump_files => options[:dump_files],
        :dump_dir => options[:dump_dir],
    })

    @solr_io = SolrIO.new(options[:solr_url], {:authorization => options[:solr_auth]})

  end

  def run
    reindex("dcararchives")
    reindex("dcararchivers")
    reindex("dcarlegislations")
  end

  private

  def reindex(collection_name)
    create_index(collection_name)
    puts "DELETE #{collection_name}"
    @solr_io.delete_data(collection_name)
    puts "UPDATE #{collection_name}"
    batch = []
    batch_size = 1000
    @timbuctoo_io.scrape_collection(collection_name, {
        :process_record => -> (record) {
          batch << @mappers[collection_name.to_sym].convert(record)
          if batch.length >= batch_size
            @solr_io.update(collection_name, batch)
            batch = []
          end
        },
        :with_relations => true,
        :from_file => @options[:from_file],
        :batch_size => 1000
    })
    @solr_io.update(collection_name, batch)
    puts "COMMIT #{collection_name}"
    @solr_io.commit(collection_name)
  end

  def create_index collection
    puts "CREATE #{collection}"
    begin
      @solr_io.create(collection)
    rescue Exception => e
      puts "Index #{collection} already exists"
    end
  end
end
