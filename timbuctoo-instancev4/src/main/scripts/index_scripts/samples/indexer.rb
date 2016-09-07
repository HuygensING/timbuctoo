require 'open-uri'

require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/default_mapper'
require '../lib/timbuctoo_solr/solr_io'


class Indexer

  def initialize
    @timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')
    @solr_io = SolrIO.new('http://localhost:8983/solr')

    @mapper = DefaultMapper.new({
        :properties => [
            { :name => '_id', :converted_name => 'id' },
            { :name => '@displayName',  :converted_name => 'displayName_s'},
            { :name => [ '^modified', 'timeStamp' ], :converted_name => 'modified_l'}
        ],
        :relations => [
            {
                :relation_name => 'has_archive_keyword', # name of the relation to follow
                :property_name => 'displayName', # get the path property to the related object
                :converted_name => 'keyword_ss' # list of strings data type
            }
        ]
    })
  end

  def run
    @solr_io.create('testing')
    @timbuctoo_io.scrape_collection('dcararchives', :with_relations => true, :process_record => method(:process))
    @solr_io.commit('testing')
    dump_result
    @solr_io.delete_index('testing')
  end

  def process(record)
    $stdout.print "."
    @solr_io.update('testing', [@mapper.convert(record)])
  end


  def dump_result
    open('http://localhost:8983/solr/testing/select?q=*:*&indent=on') {|f| f.each_line{|l| puts l }}
  end
end

Indexer.new.run