require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'
require '../lib/timbuctoo_solr/default_mapper'

require './configs/dcar_collective_config'
require './configs/dcar_person_config'
require './configs/dcar_document_config'
require './mappers/dcar_person_mapper'
require './mappers/dcar_document_mapper'
require './mappers/dcar_person_reception_mapper'
require './mappers/dcar_document_reception_mapper'

class DutchCaribbeanIndexer
  def initialize(options)
    @options = options

#    @person_mapper = DcarPersonMapper.new(DcarPersonConfig.get)
    @person_mapper = DefaultMapper.new(DcarPersonConfig.get)
#    @document_mapper = DcarDocumentMapper.new(DcarDocumentConfig.get)
    @document_mapper = DefaultMapper.new(DcarDocumentConfig.get)
    @collective_mapper = DefaultMapper.new(DcarCollectiveConfig.get)

    @person_reception_mapper = DcarPersonReceptionMapper.new(@person_mapper, @document_mapper)
    @document_reception_mapper = DcarDocumentReceptionMapper.new(@document_mapper)

    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url], {
        :dump_files => options[:dump_files],
        :dump_dir => options[:dump_dir],
    })

    @solr_io = SolrIO.new(options[:solr_url], {:authorization => options[:solr_auth]})

  end

  def run
    # Scrape persons and documents from Timbuctoo
    scrape_archives
    scrape_archivers
    scrape_legislation

    # Always run person_mapper.add_languages before @document_mapper.add_creators to ensure correct _childDocuments_
    # filters on dcardocuments index and dcardocumentreceptions index!!
#    @person_mapper.add_languages(@document_mapper)
#    @document_mapper.add_creators(@person_mapper)


#    puts "Found #{@document_mapper.person_receptions.length} person receptions"
#    puts "Found #{@document_mapper.document_receptions.length} document receptions"

    reindex_archives
    reindex_archivers
    reindex_legislation
#    reindex_person_receptions
#    reindex_document_receptions
  end

  private

  def scrape_legislation
    @timbuctoo_io.scrape_collection("dcarlegislations", {
        :with_relations => false,
        :from_file => @options[:from_file],
        :batch_size => 1000,
        :process_record => @document_mapper.method(:convert)
    })
    # No counter in default mapper
#    puts "SCRAPE: #{@document_mapper.record_count} legislations"
  end

  def scrape_archives
    @timbuctoo_io.scrape_collection("dcararchives", {
        :with_relations => false,
        :from_file => @options[:from_file],
        :batch_size => 1000,
        :process_record => @document_mapper.method(:convert)
    })
    # No counter in default mapper
#    puts "SCRAPE: #{@collective_mapper.record_count} archives"
  end

  def scrape_archivers
    @timbuctoo_io.scrape_collection("dcararchivers", {
        :with_relations => false,
        :from_file => @options[:from_file],
        :batch_size => 1000,
        :process_record => @collective_mapper.method(:convert)
    })
    # No counter in default mapper
#    puts "SCRAPE: #{@collective_mapper.record_count} archives"
  end

  def reindex_archives
    puts "DELETE archives"
    @solr_io.delete_data("dcararchives")
    puts "UPDATE archives"
    batch = []
    batch_size = 1000
    @timbuctoo_io.scrape_collection("dcararchives", {
        :process_record => -> (record) {
          batch << @document_mapper.convert(record)
          if batch.length >= batch_size
            @solr_io.update("dcararchives", batch)
            batch = []
          end
        },
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
          batch << @collective_mapper.convert(record)
          if batch.length >= batch_size
            @solr_io.update("dcararchivers", batch)
            batch = []
          end
        },
        :from_file => @options[:from_file],
        :batch_size => 1000
    })
    @solr_io.update("dcararchives", batch)
#
    puts "COMMIT archivers"
    @solr_io.commit("dcararchivers")
  end

  def reindex_legislation
    puts "DELETE legislation"
    @solr_io.delete_data("dcarlegislation")
    puts "UPDATE legislation"
    @document_mapper.send_cached_batches_to("dcarlegislation", @solr_io.method(:update))
    puts "COMMIT legislation"
    @solr_io.commit("dcarlegislation")
  end

  def reindex_person_receptions
    puts "DELETE person receptions"
    @solr_io.delete_data("dcarpersonreceptions")
    puts "UPDATE person receptions"
    update_reception_index(@person_reception_mapper, "dcarpersonreceptions", :person_receptions)
    puts "COMMIT person receptions"
    @solr_io.commit("dcarpersonreceptions")
  end

  def reindex_document_receptions
    puts "DELETE document receptions"
    @solr_io.delete_data("dcardocumentreceptions")
    puts "UPDATE document receptions"
    update_reception_index(@document_reception_mapper, "dcardocumentreceptions", :document_receptions)
    puts "COMMIT document receptions"
    @solr_io.commit("dcardocumentreceptions")
  end

  def update_reception_index(reception_mapper, index_name, reception_entry)
    batch = []
    batch_size = 500
    @document_mapper.send(reception_entry).each do |reception|
      batch << reception_mapper.convert(reception)
      if batch.length >= batch_size
        @solr_io.update(index_name, batch)
        batch = []
      end
    end
    @solr_io.update(index_name, batch)
  end
end
