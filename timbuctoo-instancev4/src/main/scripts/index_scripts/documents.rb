class Documents

    @@location = ""
    @@solr_documents = ""
    @@solr_receptions = ""
    @@debug = false
    @@number = 0
    @@documents = Hash.new
    @@person_receptions = Array.new

    def Documents.scrape_file start_value, num_of_lines=100
	result = Array.new
	STDERR.puts "start=#{start_value}"
    
	location = "#{@@location}domain/wwdocuments?rows=#{num_of_lines}&start=#{start_value}&withRelations=true"
	STDERR.puts location  if @@debug
	f = open(location, {:read_timeout=>600})
	line = f.gets
	array = JSON.parse(line)
	array.each do |obj|
	    res = Document.new(obj)
	    @@documents[res['id']] = res
	    result << res
	    @@number += 1
	    start_value += 1
	end
	STDERR.puts "#{start_value}"  if array.size < 100 && array.size > 0
	if !line.eql?("[]")
	    Documents.do_solr_update result,@@solr_documents
	    Documents.do_solr_update @@person_receptions,@@solr_receptions
	    @@person_receptions = Array.new
	end
	return !line.eql?("[]")
    end


    def Documents.do_solr_update batch,location
	STDERR.puts "batch.size: #{batch.size}"  if @@debug
	uri = URI.parse("#{location}update/")
	STDERR.puts "uri: #{uri}"  if @@debug
	req = Net::HTTP::Post.new(uri)
	req.content_type = "application/json"
	http = Net::HTTP.new(uri.hostname, uri.port)
	req.body = batch.to_json
	result = http.request(req)
	STDERR.puts "result 1: #{result}" if @@debug
    end

    def Documents.solr_commit location
	uri = URI.parse("#{location}update?commit=true")
	STDERR.puts "uri: #{uri}"  if @@debug
	req = Net::HTTP::Post.new(uri)
	http = Net::HTTP.new(uri.hostname, uri.port)
	result = http.request(req)
	STDERR.puts "result 2: #{result}" if @@debug
    end

    def Documents.location= location
	@@location = location
    end

    def Documents.solr_documents= solr
	@@solr_documents = solr
    end

    def Documents.solr_receptions= solr
	@@solr_receptions = solr
    end

    def Documents.person_receptions_concat data
	STDERR.puts "new data size: #{data.size}"  if @@debug
	@@person_receptions += data
	STDERR.puts "res size: #{@@person_receptions.size}"   if @@debug
    end

    def Documents.debug= debug
	@@debug = debug
    end

    def Documents.number
	@@number
    end

end

