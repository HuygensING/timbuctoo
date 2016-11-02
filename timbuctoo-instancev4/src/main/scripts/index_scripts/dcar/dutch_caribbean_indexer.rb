require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'
require '../lib/timbuctoo_solr/default_mapper'

require './configs/dcar_archive_config'
require './configs/dcar_archiver_config'
require './configs/dcar_legislation_config'
require './mappers/dcar_mapper'

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
    # Scrape archives, archivers and legislation from Timbuctoo
    # scrape_archives
    # scrape_archivers
    # scrape_legislation

    reindex_archives
    reindex_archivers
    reindex_legislation
  end

  private

  def scrape_legislation
    @timbuctoo_io.scrape_collection("dcarlegislations", {
        :with_relations => true,
        :from_file => @options[:from_file],
        :batch_size => 1000,
        :process_record => @legislation_mapper.method(:convert)
    })
    puts "SCRAPE: #{@legislation_mapper.record_count} legislations"
  end

  def scrape_archives
    @timbuctoo_io.scrape_collection("dcararchives", {
        :with_relations => true,
        :from_file => @options[:from_file],
        :batch_size => 1000,
        :process_record => @archive_mapper.method(:convert)
    })
    puts "SCRAPE: #{@archive_mapper.record_count} archives"
  end

  def scrape_archivers
    @timbuctoo_io.scrape_collection("dcararchivers", {
        :with_relations => true,
        :from_file => @options[:from_file],
        :batch_size => 1000,
        :process_record => @archiver_mapper.method(:convert)
    })
    puts "SCRAPE: #{@archiver_mapper.record_count} archives"
  end

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
