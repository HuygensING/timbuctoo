require 'optparse'

require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'
require '../lib/timbuctoo_solr/default_mapper'
require '../lib/mixins/converters/to_year_converter'
require '../lib/mixins/converters/to_names_converter'

require './configs/ww_person_config'

class WwPersonMapper < DefaultMapper
  include ToYearConverter
  include ToNamesConverter
end

class WwDocumentMapper < DefaultMapper
  include ToYearConverter
end


options = {}

OptionParser.new do |opts|
  opts.on('-f', '--from-file', 'Scrape timbuctoo from local file cache') {|f| options[:from_file] = true }
  opts.on('-t', '--timbuctoo-url TIM_URL', 'Base url for Timbuctoo') {|t| options[:timbuctoo_url] = t }
  opts.on('-s', '--solr-url SOLR_URL', 'Base url for Timbuctoo') {|s| options[:solr_url] = s }
  opts.on('-a', '--solr-auth AUTH', 'Value for Authentication header of solr server') {|a| options[:solr_auth] = a }
end.parse!


p options
