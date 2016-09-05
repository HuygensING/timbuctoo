require File.dirname(__FILE__) + '/../../lib/timbuctoo_solr/default_mapper'
require File.dirname(__FILE__) + '/../../lib/mixins/converters/to_year_converter'


class WwDocumentMapper < DefaultMapper
  include ToYearConverter

  attr_reader :cache, :record_count

  def initialize(options)
    super options
    @cache = {}
    @record_count = 0
  end

  def convert(record)
    data = super(record)
    data['type_s'] = 'document'
    add_english_title(data)

    puts "document scrape: #{@record_count}" if @record_count % 100 == 0
    @record_count += 1
    @cache[data['id']] = data
  end

  def add_creators(person_mapper)
    @cache.each do |id, record|
      @cache[id]['_childDocuments_'] = []
      name_sorts = Array.new
      @cache[id]['authorGender_ss'] = Array.new
      @cache[id]['authorName_ss'] = Array.new
      @cache[id]['authorNameSort_s'] = ''

      record['@authorIds'].each do |author_id|
        author = person_mapper.find(author_id)
        if author.nil?
          $stderr.puts "WARNING Problem finding wwperson #{author_id} which isCreatorOf #{id} (wrong VRE?)"
        else
          child_author = {}
          author.each do |key, value|
            child_author["person_#{key}"] = value unless key.eql?("id")
          end
          child_author["id"] = "#{id}/#{author_id}"
          @cache[id]['authorGender_ss'] << author['gender_s']
          @cache[id]['authorName_ss'] << author['displayName_s']
          name_sorts << author['nameSort_s']
          @cache[id]['_childDocuments_'] << child_author
        end
      end

      @cache[id]['authorNameSort_s'] = name_sorts.sort.first if name_sorts.length > 0
      @cache[id].delete('@authorIds')
    end
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

  private
  def add_english_title(data)
    unless data['^englishTitle'].nil?
      data['title_t'] = data['title_t'].nil? ? data['^englishTitle'] : "#{data['title_t']} #{data['^englishTitle']}"
      data.delete('^englishTitle')
    end
  end

end