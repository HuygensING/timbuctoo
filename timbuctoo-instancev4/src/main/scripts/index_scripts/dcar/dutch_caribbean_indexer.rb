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

    @legislation_mapper = DcarMapper.new(DcarLegislationConfig.get)
    @archive_mapper = DcarMapper.new(DcarArchiveConfig.get)
    @archiver_mapper = DcarMapper.new(DcarArchiverConfig.get)

    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url], {
        :dump_files => options[:dump_files],
        :dump_dir => options[:dump_dir],
    })

    @solr_io = SolrIO.new(options[:solr_url], {:authorization => options[:solr_auth]})

  end

  def run
    reindex_archives
    reindex_archivers
    reindex_legislation
  end

  private

  def reindex_archives
    collection_name = "dcararchives"
    create_index(collection_name)
    puts "DELETE dcararchives"
    @solr_io.delete_data(collection_name)
    puts "UPDATE dcararchives"
    batch = []
    batch_size = 1000
    @timbuctoo_io.scrape_collection(collection_name, {
        :process_record => -> (record) {
          batch << @archive_mapper.convert(record)
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
    puts "COMMIT dcararchives"
    @solr_io.commit(collection_name)
  end

  def reindex_archivers
    collection_name = "dcararchivers"
    create_index(collection_name)
    puts "DELETE dcararchivers"
    @solr_io.delete_data(collection_name)
    puts "UPDATE dcararchivers"

    batch = []
    batch_size = 1000
    @timbuctoo_io.scrape_collection(collection_name, {
        :process_record => -> (record) {
          batch << @archiver_mapper.convert(record)
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
    puts "COMMIT dcararchivers"
    @solr_io.commit(collection_name)
  end

  def reindex_legislation
    collection_name = "dcarlegislations"
    create_index(collection_name)
    puts "DELETE dcarlegislations"
    @solr_io.delete_data(collection_name)
    puts "UPDATE dcarlegislations"

    batch = []
    batch_size = 1000
    @timbuctoo_io.scrape_collection(collection_name, {
        :process_record => -> (record) {
          batch << @legislation_mapper.convert(record)
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
    puts "COMMIT dcarlegislations"
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
