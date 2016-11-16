require 'optparse'
require './federated_indexer'

options = {
  :forked => false,
  :debug_sample => false
}

OptionParser.new do |opts|
  opts.on('-d', '--dump-dir DIR', 'Save dump of scraped Timbuctoo into a dir') {|d| options[:dump_files] = true; options[:dump_dir] = d }
  opts.on('-f', '--from-file', 'Scrape timbuctoo from local file cache') {|f| options[:from_file] = true }
  opts.on('-t', '--timbuctoo-url TIM_URL', 'Base url for Timbuctoo') {|t| options[:timbuctoo_url] = t }
  opts.on('-s', '--solr-url SOLR_URL', 'Base url for Timbuctoo') {|s| options[:solr_url] = s }
  opts.on('-a', '--solr-auth AUTH', 'Value for Authentication header of solr server') {|a| options[:solr_auth] = a }
  opts.on('-i', '--index-name NAME', 'Name of the index, defaults to "federated"') {|i| options[:index_name] = i }
  opts.on('-F', '--forked', 'run in forked mode, one fork per collection') {|x| options[:forked] = true }
  opts.on('-D', '--debug-sample', 'run in forked mode, one fork per collection') {|x| options[:debug_sample] = true }
end.parse!


FederatedIndexer.new(options).run