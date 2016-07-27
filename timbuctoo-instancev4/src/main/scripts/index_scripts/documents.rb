class Documents

    @@location = ""
    @@solr_documents = ""
    @@solr_receptions = ""
    @@solr_doc_receptions = ""
    @@debug = false
    @@number = 0
    @@documents = Hash.new
    @@document_receptions = Array.new
    @@complete_document_receptions = Array.new
    @@person_receptions = Array.new

    def Documents.scrape_file start_value, num_of_lines=100
	location = "#{@@location}domain/wwdocuments?rows=#{num_of_lines}&start=#{start_value}&withRelations=true"
	STDERR.puts location  if @@debug
	f = open(location, {:read_timeout=>600})
	line = f.gets
	return false  if line.eql?("[]")

     	rest = start_value.modulo(1000)
	if rest==0
	    STDERR.print start_value/1000
	elsif rest==500
	    STDERR.print "+"
	else
	    STDERR.print "."
	end

	result = Array.new
	array = JSON.parse(line)
	array.each do |obj|
	    res = Document.new(obj)
	    @@documents[res['id']] = res['_childDocuments_']
	    result << res
	    @@number += 1
	    start_value += 1
	end
	if !line.eql?("[]")
	    Documents.do_solr_update result,@@solr_documents
	    Documents.do_solr_update @@person_receptions,@@solr_receptions
	    # dit komt kennelijk nooit voor?
	    if @@complete_document_receptions.size >= 200
		puts "#{@@complete_document_receptions.size} document receptions worden geschreven"
		Documents.do_solr_update @@complete_document_receptions,
		    @@solr_doc_receptions
		@@complete_document_receptions = Array.new
	    end
	    @@person_receptions = Array.new
	end
	return !line.eql?("[]")
    end


    def Documents.do_solr_update batch,location,debug=false
	STDERR.puts "batch.size: #{batch.size}"  if @@debug || debug
	uri = URI.parse("#{location}update/")
	STDERR.puts "uri: #{uri}"  if @@debug
	req = Net::HTTP::Post.new(uri)
	req.content_type = "application/json"
	http = Net::HTTP.new(uri.hostname, uri.port)
	req.body = batch.to_json
	result = http.request(req)
	STDERR.puts "result 1: #{result}" if @@debug || debug
    end

    def Documents.solr_commit location, debug=false
	uri = URI.parse("#{location}update?commit=true")
	STDERR.puts "uri: #{uri}"  if @@debug || debug
	req = Net::HTTP::Post.new(uri)
	http = Net::HTTP.new(uri.hostname, uri.port)
	result = http.request(req)
	STDERR.puts "result 2: #{result}" if @@debug || debug
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

    def Documents.solr_receptions
	@@solr_receptions
    end

    def Documents.solr_doc_receptions= solr
	@@solr_doc_receptions = solr
    end

    def Documents.solr_doc_receptions
	@@solr_doc_receptions
    end

    def Documents.person_receptions_concat data
	STDERR.puts "new data size: #{data.size}"  if @@debug
	@@person_receptions += data
	STDERR.puts "res size: #{@@person_receptions.size}"   if @@debug
    end

    def Documents.document_receptions_concat data
	STDERR.puts "new data size: #{data.size}"  if @@debug
	@@document_receptions += data
	STDERR.puts "res size: #{@@document_receptions.size}"   if @@debug
    end

    def Documents.document_receptions
	@@document_receptions
    end

    def Documents.complete_document_receptions_add data
	@@complete_document_receptions << data
	STDERR.puts "res size: #{@@complete_document_receptions.size}"   if @@debug
    end

    def Documents.complete_document_receptions
	@@complete_document_receptions
    end

    def Documents.debug= debug
	@@debug = debug
    end

    def Documents.number
	@@number
    end

    def Documents.find id
	@@documents[id]
    end

end

