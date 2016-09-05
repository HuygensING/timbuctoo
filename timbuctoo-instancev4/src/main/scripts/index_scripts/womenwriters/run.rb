require 'optparse'

require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'

require './configs/ww_person_config'
require './configs/ww_document_config'
require './mappers/ww_person_mapper'
require './mappers/ww_document_mapper'
require './mappers/ww_person_reception_mapper'
require './mappers/ww_document_reception_mapper'

options = {}

OptionParser.new do |opts|
  opts.on('-d', '--dump-dir DIR', 'Save dump of scraped Timbuctoo into a dir') {|d| options[:dump_files] = true; options[:dump_dir] = d }
  opts.on('-f', '--from-file', 'Scrape timbuctoo from local file cache') {|f| options[:from_file] = true }
  opts.on('-t', '--timbuctoo-url TIM_URL', 'Base url for Timbuctoo') {|t| options[:timbuctoo_url] = t }
  opts.on('-s', '--solr-url SOLR_URL', 'Base url for Timbuctoo') {|s| options[:solr_url] = s }
  opts.on('-a', '--solr-auth AUTH', 'Value for Authentication header of solr server') {|a| options[:solr_auth] = a }
end.parse!


class WomenWritersIndexer
  def initialize(options)
    @options = options

    @person_mapper = WwPersonMapper.new(WwPersonConfig.get)
    @document_mapper = WwDocumentMapper.new(WwDocumentConfig.get)
    @person_reception_mapper = WwPersonReceptionMapper.new(@person_mapper, @document_mapper)
    @document_reception_mapper = WwDocumentReceptionMapper.new(@document_mapper)

    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url], {
        :dump_files => options[:dump_files],
        :dump_dir => options[:dump_dir],
    })

    @solr_io = SolrIO.new(options[:solr_url], {:authorization => options[:solr_auth]})

  end

  def run
    # Scrape persons and documents from Timbuctoo
    scrape_persons
    scrape_documents

    # Always run person_mapper.add_languages before @document_mapper.add_creators to ensure correct _childDocuments_
    # filters on wwdocuments index and wwdocumentreceptions index!!
    @person_mapper.add_languages(@document_mapper)
    @document_mapper.add_creators(@person_mapper)


    puts "Found #{@document_mapper.person_receptions.length} person receptions"
    puts "Found #{@document_mapper.document_receptions.length} document receptions"


    puts "DELETE persons"
    @solr_io.delete_data("wwpersons")
    puts "UPDATE persons"
    @person_mapper.send_cached_batches_to("wwpersons", @solr_io.method(:update))
    puts "COMMIT persons"
    @solr_io.commit("wwpersons")

    puts "DELETE documents"
    @solr_io.delete_data("wwdocuments")
    puts "UPDATE documents"
    @document_mapper.send_cached_batches_to("wwdocuments", @solr_io.method(:update))
    puts "COMMIT documents"
    @solr_io.commit("wwdocuments")


    puts "DELETE person receptions"
    @solr_io.delete_data("wwpersonreceptions")
    puts "UPDATE person receptions"
    update_reception_index(@person_reception_mapper, "wwpersonreceptions", :person_receptions)
    puts "COMMIT person receptions"
    @solr_io.commit("wwpersonreceptions")

    puts "DELETE document receptions"
    @solr_io.delete_data("wwdocumentreceptions")
    puts "UPDATE document receptions"
    update_reception_index(@document_reception_mapper, "wwdocumentreceptions", :document_receptions)
    puts "COMMIT document receptions"
    @solr_io.commit("wwdocumentreceptions")

  end

  private
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

  def scrape_documents
    @timbuctoo_io.scrape_collection("wwdocuments", {
        :with_relations => true,
        :from_file => @options[:from_file],
        :process_record => @document_mapper.method(:convert)
    })
    puts "SCRAPE: #{@document_mapper.record_count} documents"
  end

  def scrape_persons
    @timbuctoo_io.scrape_collection("wwpersons", {
        :with_relations => true,
        :from_file => @options[:from_file],
        :process_record => @person_mapper.method(:convert)
    })
    puts "SCRAPE: #{@person_mapper.record_count} persons"
  end

end


WomenWritersIndexer.new(options).run