require 'open-uri'
require '../lib/timbuctoo_solr/solr_io'

# Initialize for local solr
solr_io = SolrIO.new('http://localhost:8983/solr')

# Create index named 'testing'
solr_io.create('testing')

# Update index with batch of one record
solr_io.update('testing', [{:id => "foobar", :value_i => 123}])
solr_io.commit('testing')


puts "After update + commit\r\n==="
# Show query results
open('http://localhost:8983/solr/testing/select?q=*:*&indent=on') {|f| f.each_line{|l| puts l }}

# Throw away the data
solr_io.delete_data('testing')

puts "After delete\r\n==="
open('http://localhost:8983/solr/testing/select?q=*:*&indent=on') {|f| f.each_line{|l| puts l }}

solr_io.commit('testing')

puts "After commit\r\n==="
open('http://localhost:8983/solr/testing/select?q=*:*&indent=on') {|f| f.each_line{|l| puts l }}



solr_io.delete_index('testing')