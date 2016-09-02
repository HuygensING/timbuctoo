require 'net/http'
require 'json'

class TimbuctooIO

  attr_accessor :base_url, :dump_dir, :dump_files

  def initialize (base_url, options = {:dump_files => false, :dump_dir => nil})
    @base_url = base_url
    @dump_files = options[:dump_files]
    @dump_dir = options[:dump_dir]
  end


  def scrape_collection (collection_name,
                         process_record = lambda {|record| puts record.inspect },
                         with_relations = false,
                         batch_size = 100)

    start_value = 0
    data = nil
    while data.nil? or data.length > 0
      location = "#{@base_url}/v2.1/domain/#{collection_name}" +
          "?rows=#{batch_size}" +
          "&start=#{start_value}" +
          (with_relations ? '&withRelations=true' : '')
      uri = URI.parse(location)
      req = Net::HTTP::Get.new(uri)
      http = Net::HTTP.new(uri.hostname, uri.port)

      response = http.request(req)

      raise "http request to failed with status #{response.code}: #{location}" unless response.code.eql?("200")

      dump_to_file(collection_name, batch_size, start_value, with_relations, response.body) if dump_files and !dump_dir.nil?
      data = JSON.parse(response.body)

      data.each {|record| process_record.call(record) }

      start_value = start_value + batch_size
    end
  end

  def dump_to_file (collection_name, batch_size, start_value, with_relations, response_body)
    filename = "#{dump_dir.sub(/\/$/, '')}/#{collection_name}_rows_#{batch_size}_start_#{start_value}_#{with_relations ? "with_relations" : ""}.json"
    File.open(filename, 'w') { |file| file.write(response_body) }
  end

end