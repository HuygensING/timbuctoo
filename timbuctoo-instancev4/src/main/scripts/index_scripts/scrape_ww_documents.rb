require 'open-uri'
require 'pp'
require './timer.rb'
require './person.rb'
require './document.rb'
require 'json'


def scrape_file start_value, num_of_lines=100
    result = Array.new
    STDERR.puts "start=#{start_value}"

    location = "#{@location}v2.1/domain/wwdocuments?rows=#{num_of_lines}&start=#{start_value}&withRelations=true"
    f = open(location, {:read_timeout=>600})
    line = f.gets
    array = JSON.parse(line)
    array.each do |obj|
	result << Document.new(obj).document
	puts result.last.to_json if @debug
	start_value += 1
    end
    STDERR.puts "#{start_value}"  if array.size < 100 && array.size > 0
    if !line.eql?("[]")
	do_solr_post result
    end
    return !line.eql?("[]")
end


def do_solr_post batch
    uri = URI.parse("#{@solr}update/")
    req = Net::HTTP::Post.new(uri)
    req.content_type = "application/json"
    http = Net::HTTP.new(uri.hostname, uri.port)
    req.body = batch.to_json
    result = http.request(req)
    uri = URI.parse("#{@solr}update?commit=true")
    req = Net::HTTP::Post.new(uri)
    http = Net::HTTP.new(uri.hostname, uri.port)
    result = http.request(req)
end


if __FILE__ == $0
    Timer.start

    @debug = false
    output_dir = ""
    multiple_archives = ""
    @location = "http://test.repository.huygens.knaw.nl/"
    @collection = "gettingstarted/"
#    @collection = "wwdocuments/"
    @solr = "http://192.168.99.100:8983/solr/#{@collection}"
#    @solr = "http://10.152.32.34:8983/solr/#{@collection}"

    begin
	(0..(ARGV.size-1)).each do |i|
	case ARGV[i]
	    when '--debug'
		@debug = true
	    when '-coll'
		@collection = ARGV[i+1]
	    when '-loc'
		@location = ARGV[i+1]
	    when '-solr'
		@solr = "#{ARGV[i+1]}#{@collection}"
	    when '-h'
		STDERR.puts "use: ruby scrape_ww_documents.rb -coll collection -loc location -solr solr-site [--debug]"
		exit(1)
	end
    end
    rescue => detail
	STDERR.puts "#{detail}"
    end
    
    Document.location = "#{@location}v2.1/"

    continu = true
    start_value = 0
    num_of_lines = @debug ? 10 : 100
    while(continu)
	continu = scrape_file start_value,num_of_lines
	continu = false if @debug && start_value==900
	start_value += 100 if continu
    end

    Timer.stop
end

