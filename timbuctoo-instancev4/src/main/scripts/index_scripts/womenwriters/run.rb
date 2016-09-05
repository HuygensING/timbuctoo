require 'optparse'

require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'

require './configs/ww_person_config'
require './configs/ww_document_config'
require './mappers/ww_person_mapper'
require './mappers/ww_document_mapper'

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

    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url], {
        :dump_files => options[:dump_files],
        :dump_dir => options[:dump_dir],
    })

    @solr_io = SolrIO.new('http://localhost:8983/solr', {:authorization => options[:solr_auth]})

  end

  def run
    @timbuctoo_io.scrape_collection("wwpersons", {
        :with_relations => true,
        :from_file => @options[:from_file],
        :process_record => @person_mapper.method(:convert)
    })
    puts "SCRAPE: #{@person_mapper.record_count} persons"

    @timbuctoo_io.scrape_collection("wwdocuments", {
        :with_relations => true,
        :from_file => @options[:from_file],
        :process_record => @document_mapper.method(:convert)
    })
    puts "SCRAPE: #{@document_mapper.record_count} documents"

    # Always run person_mapper.add_languages before @document_mapper.add_creators to ensure correct _childDocuments_
    # filters on wwdocuments index and wwdocumentreceptions index!!
    @person_mapper.add_languages(@document_mapper)
    @document_mapper.add_creators(@person_mapper)


    puts "DELETE persons"
    @solr_io.delete_data("wwperson_test")
    puts "UPDATE persons"
    @person_mapper.send_cached_batches_to("wwperson_test", @solr_io.method(:update))
    puts "COMMIT persons"
    @solr_io.commit("wwperson_test")

    puts "DELETE documents"
    @solr_io.delete_data("wwdocument_test")
    puts "UPDATE documents"
    @document_mapper.send_cached_batches_to("wwdocument_test", @solr_io.method(:update))
    puts "COMMIT documents"
    @solr_io.commit("wwdocument_test")
  end
end


WomenWritersIndexer.new(options).run

# timbuctoo_io.scrape_collection("wwpersons", { :with_relations => true })
# timbuctoo_io.scrape_collection("wwdocuments", { :with_relations => true })