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

    @person_mapper = DcarPersonMapper.new(DcarPersonConfig.get)
    @document_mapper = DcarDocumentMapper.new(DcarDocumentConfig.get)
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
#    scrape_documents

    # Always run person_mapper.add_languages before @document_mapper.add_creators to ensure correct _childDocuments_
    # filters on dcardocuments index and dcardocumentreceptions index!!
    @person_mapper.add_languages(@document_mapper)
    @document_mapper.add_creators(@person_mapper)


    puts "Found #{@document_mapper.person_receptions.length} person receptions"
    puts "Found #{@document_mapper.document_receptions.length} document receptions"

    reindex_archives
#    reindex_persons
#    reindex_documents
#    reindex_person_receptions
#    reindex_document_receptions
  end

  private

  def scrape_documents
    @timbuctoo_io.scrape_collection("dcardocuments", {
        :with_relations => true,
        :from_file => @options[:from_file],
        :process_record => @document_mapper.method(:convert)
    })
    puts "SCRAPE: #{@document_mapper.record_count} documents"
  end

  def scrape_archives
    @timbuctoo_io.scrape_collection("dcararchives", {
        :with_relations => false,
        :from_file => @options[:from_file],
        :process_record => @person_mapper.method(:convert)
    })
    puts "SCRAPE: #{@person_mapper.record_count} archives"
  end

  def reindex_archives
    puts "DELETE archives"
#    @solr_io.delete_data("dcararchives")
    puts "UPDATE archives"
    batch = []
    batch_size = 1000
    STDERR.puts "from_file: #{@options[:from_file]}"
    @timbuctoo_io.scrape_collection("dcararchives", {
        :process_record => -> (record) {
          batch << @collective_mapper.convert(record)
          if batch.length >= batch_size
            @solr_io.update("dcararchives", batch)
            batch = []
          end
        },
        :from_file => @options[:from_file]
    })
    @solr_io.update("dcararchives", batch)
    puts "COMMIT archives"
    @solr_io.commit("dcararchives")
  end

  def reindex_persons
    puts "DELETE persons"
    @solr_io.delete_data("dcarpersons")
    puts "UPDATE persons"
    @person_mapper.send_cached_batches_to("dcarpersons", @solr_io.method(:update))
    puts "COMMIT persons"
    @solr_io.commit("dcarpersons")
  end

  def reindex_documents
    puts "DELETE documents"
    @solr_io.delete_data("dcardocuments")
    puts "UPDATE documents"
    @document_mapper.send_cached_batches_to("dcardocuments", @solr_io.method(:update))
    puts "COMMIT documents"
    @solr_io.commit("dcardocuments")
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
