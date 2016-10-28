require File.dirname(__FILE__) + '/../../lib/timbuctoo_solr/default_mapper'
require File.dirname(__FILE__) + '/../../lib/mixins/converters/to_year_converter'
require File.dirname(__FILE__) + '/../../lib/mixins/converters/to_names_converter'

class DcarArchiverMapper < DefaultMapper
  include ToYearConverter

  attr_reader :cache, :record_count

  def initialize options
    super options
    @cache = {}
    @record_count = 0
  end

  def convert(record)
    data = super(record)
    data['type_s'] = 'archive'

#    puts "Archive scrape: #{@record_count}" if @record_count % 100 == 0
    @record_count += 1
    @cache[data['id']] = data
  end

  def find(id)
    @cache[id]
  end

  def send_cached_batches_to(index_name, batch_callback)
    batch_size = 500
    batch = []
    @cache.each do |key, record|
      batch << record
      if batch.length >= batch_size
        batch_callback.call(index_name, batch)
        batch = []
      end
    end
    batch_callback.call(index_name, batch)
  end

end
