require 'open-uri'
require 'pp'
require './timer.rb'
require './person.rb'
require 'json'
require 'net/http'

def scrape_file start_value, num_of_lines=100
    result = Array.new
    STDERR.puts "start=#{start_value}"

    location = "http://test.repository.huygens.knaw.nl/v2.1/domain/wwpersons?rows=#{num_of_lines}&start=#{start_value}"
    f = open(location)
    line = f.gets
    array = JSON.parse(line)
    array.each do |obj|
	result << Person.build_person(obj)
	start_value += 1
    end
    STDERR.puts "#{start_value}"  if array.size < 100 && array.size > 0
    if !line.eql?("[]")
	do_solr_post result
    end
    return !line.eql?("[]")
end


def do_solr_post batch
    uri = URI.parse("http://192.168.99.100:8983/solr/gettingstarted/update/")
    req = Net::HTTP::Post.new(uri)
    req.content_type = "application/json"
    http = Net::HTTP.new(uri.hostname, uri.port)
    req.body = batch.to_json
    result = http.request(req)
    puts "result (add json): #{result}"
    uri = URI.parse("http://192.168.99.100:8983/solr/gettingstarted/update?commit=true")
    req = Net::HTTP::Post.new(uri)
    http = Net::HTTP.new(uri.hostname, uri.port)
    result = http.request(req)
    puts "result (update): #{result}"
end


if __FILE__ == $0
    Timer.start

    debug = false
    output_dir = ""
    multiple_archives = ""
    begin
    (0..(ARGV.size-1)).each do |i|
	case ARGV[i]
	    when '--debug'
		debug = true
	    when '-h'
		STDERR.puts "use: ruby scrape_ww_persons.rb [--debug]"
		exit(1)
	end
    end
    rescue => detail
	STDERR.puts "#{detail}"
    end

    continu = true
    start_value = 0
    all_ww_persons = Array.new
    while(continu)
	continu = scrape_file start_value
	start_value += 100 if continu
    end

    Timer.stop
end

