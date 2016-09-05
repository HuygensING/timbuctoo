require 'optparse'

require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'
require '../lib/mixins/converters/to_year_converter'
require '../lib/mixins/converters/to_names_converter'

require './configs/ww_person_config'
require './mappers/ww_person_mapper'

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
    @timbuctoo_io = TimbuctooIO.new(options[:timbuctoo_url], {
        :dump_files => options[:dump_files],
        :dump_dir => options[:dump_dir],
    })

#    @document_mapper = WwDocumentMapper.new
  end

  def run
    @timbuctoo_io.scrape_collection("wwpersons", {
        :with_relations => true,
        :from_file => @options[:from_file],
        :process_record => @person_mapper.method(:convert)
    })

    p @person_mapper.find ("fa0920d0-83a9-4522-9be3-79b39c4bd311")
  end
end


WomenWritersIndexer.new(options).run

# timbuctoo_io.scrape_collection("wwpersons", { :with_relations => true })
# timbuctoo_io.scrape_collection("wwdocuments", { :with_relations => true })