require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'
require '../lib/timbuctoo_solr/default_mapper'

require './configs/dcar_archive_config'
require './configs/dcar_archiver_config'
require './configs/dcar_legislation_config'
require './mappers/dcar_archive_mapper'
require './mappers/dcar_archiver_mapper'
require './mappers/dcar_mapper'

class DutchCaribbeanIndexer
  def initialize(options)
    @options = options

    @legislation_mapper = DcarMapper.new(DcarLegislationConfig.get)
    @archive_mapper = DcarArchiveMapper.new(DcarArchiveConfig.get)
    @archiver_mapper = DcarArchiverMapper.new(DcarArchiverConfig.get)

#    @person_reception_mapper = DcarPersonReceptionMapper.new(@person_mapper, @document_mapper)
#    @document_reception_mapper = DcarDocumentReceptionMapper.new(@document_mapper)

    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url], {
        :dump_files => options[:dump_files],
        :dump_dir => options[:dump_dir],
    })

    @solr_io = SolrIO.new(options[:solr_url], {:authorization => options[:solr_auth]})

  end

  def run
    # Scrape archives, archivers and legislation from Timbuctoo
    scrape_archives
    scrape_archivers
    scrape_legislation

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
    # No counter in default mapper
    puts "SCRAPE: #{@archive_mapper.record_count} archives"
  end

  def scrape_archivers
    @timbuctoo_io.scrape_collection("dcararchivers", {
        :with_relations => true,
        :from_file => @options[:from_file],
        :batch_size => 1000,
        :process_record => @archiver_mapper.method(:convert)
    })
    # No counter in default mapper
    puts "SCRAPE: #{@archiver_mapper.record_count} archives"
  end

  def reindex_archives
    puts "DELETE archives"
    @solr_io.delete_data("dcararchives")
    puts "UPDATE archives"
    batch = []
    batch_size = 1000
    @timbuctoo_io.scrape_collection("dcararchives", {
        :process_record => -> (record) {
          batch << @archive_mapper.convert(record)
          if batch.length >= batch_size
            @solr_io.update("dcararchives", batch)
            batch = []
          end
        },
        :with_relations => true,
        :from_file => @options[:from_file],
        :batch_size => 1000
    })
    @solr_io.update("dcararchives", batch)
    puts "COMMIT archives"
    @solr_io.commit("dcararchives")
  end

  def reindex_archivers
    puts "DELETE archivers"
    @solr_io.delete_data("dcararchivers")
    puts "UPDATE archivers"
# not available in collective_mapper
#    @collective_mapper.send_cached_batches_to("dcararchivers", @solr_io.method(:update))
# copied from: reindex_archives
    batch = []
    batch_size = 1000
    @timbuctoo_io.scrape_collection("dcararchivers", {
        :process_record => -> (record) {
          batch << @archiver_mapper.convert(record)
          if batch.length >= batch_size
            @solr_io.update("dcararchivers", batch)
            batch = []
          end
        },
        :with_relations => true,
        :from_file => @options[:from_file],
        :batch_size => 1000
    })
    @solr_io.update("dcararchivers", batch)
#
    puts "COMMIT archivers"
    @solr_io.commit("dcararchivers")
  end

  def reindex_legislation
    puts "DELETE legislation"
    @solr_io.delete_data("dcarlegislation")
    puts "UPDATE legislation"
    # not available in document_mapper
#    @document_mapper.send_cached_batches_to("dcarlegislation", @solr_io.method(:update))
    batch = []
    batch_size = 1000
    @timbuctoo_io.scrape_collection("dcarlegislations", {
        :process_record => -> (record) {
          batch << @legislation_mapper.convert(record)
          if batch.length >= batch_size
            @solr_io.update("dcarlegislation", batch)
            batch = []
          end
        },
        :with_relations => true,
        :from_file => @options[:from_file],
        :batch_size => 1000
    })
    @solr_io.update("dcarlegislation", batch)
 #
    puts "COMMIT legislation"
    @solr_io.commit("dcarlegislation")
  end

#  def reindex_person_receptions
#    puts "DELETE person receptions"
#    @solr_io.delete_data("dcarpersonreceptions")
#    puts "UPDATE person receptions"
#    update_reception_index(@person_reception_mapper, "dcarpersonreceptions", :person_receptions)
#    puts "COMMIT person receptions"
#    @solr_io.commit("dcarpersonreceptions")
#  end

#  def reindex_document_receptions
#    puts "DELETE document receptions"
#    @solr_io.delete_data("dcardocumentreceptions")
#    puts "UPDATE document receptions"
#    update_reception_index(@document_reception_mapper, "dcardocumentreceptions", :document_receptions)
#    puts "COMMIT document receptions"
#    @solr_io.commit("dcardocumentreceptions")
#  end

#  def update_reception_index(reception_mapper, index_name, reception_entry)
#    batch = []
#    batch_size = 500
#    @document_mapper.send(reception_entry).each do |reception|
#      batch << reception_mapper.convert(reception)
#      if batch.length >= batch_size
#        @solr_io.update(index_name, batch)
#        batch = []
#      end
#    end
#    @solr_io.update(index_name, batch)
#  end
end
