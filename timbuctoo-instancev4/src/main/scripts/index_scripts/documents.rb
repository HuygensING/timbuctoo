class Documents

    @@location = ""
    @@solr = ""
    @@debug = false
    @@number = 0

    def Documents.scrape_file start_value, num_of_lines=100
	result = Array.new
	STDERR.puts "start=#{start_value}"
    
	location = "#{@@location}domain/wwdocuments?rows=#{num_of_lines}&start=#{start_value}&withRelations=true"
	STDERR.puts location  if @@debug
	f = open(location, {:read_timeout=>600})
	line = f.gets
	array = JSON.parse(line)
	array.each do |obj|
	    result << Document.new(obj)
	    @@number += 1
	    start_value += 1
	end
	STDERR.puts "#{start_value}"  if array.size < 100 && array.size > 0
	if !line.eql?("[]")
	    Documents.do_solr_post result
	end
	return !line.eql?("[]")
    end
    
    
    def Documents.do_solr_post batch
	puts batch.to_json if @@debug
	batch.each do |b|
	    puts b.to_json if @@debug
	end
	uri = URI.parse("#{@@solr}update/")
	req = Net::HTTP::Post.new(uri)
	req.content_type = "application/json"
	http = Net::HTTP.new(uri.hostname, uri.port)
	req.body = batch.to_json
	result = http.request(req)
	STDERR.puts "result 1: #{result}" if @@debug
	uri = URI.parse("#{@@solr}update?commit=true")
	req = Net::HTTP::Post.new(uri)
	http = Net::HTTP.new(uri.hostname, uri.port)
	result = http.request(req)
	STDERR.puts "result 2: #{result}" if @@debug
    end
    
    def Documents.location= location
	@@location = location
    end

    def Documents.solr= solr
	@@solr = solr
    end

    def Documents.debug= debug
	@@debug = debug
    end

    def Documents.number
	@@number
    end

end

