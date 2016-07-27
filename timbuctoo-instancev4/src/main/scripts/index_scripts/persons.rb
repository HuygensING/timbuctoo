class Persons

    @@location = ""
    @@solr = ""
    @@debug = false
    @@persons = Hash.new

    def Persons.scrape_file start_value, num_of_lines=100
	location = "#{@@location}domain/wwpersons?rows=#{num_of_lines}&start=#{start_value}&withRelations=true"
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
	    person = Person.new(obj)
	    puts "#{person.id}: #{person}" if @@debug
	    @@persons[person.id] = person
	    result << person
	    start_value += 1
	end
	Persons.do_solr_post result
	return !line.eql?("[]")
    end
    

    def Persons.do_solr_post batch
	STDERR.puts "batch: #{batch.size}" if @@debug
	STDERR.puts "solr: #{@@solr}" if @@debug
	uri = URI.parse("#{@@solr}update/")
	STDERR.puts "uri: #{uri}" if @@debug
	req = Net::HTTP::Post.new(uri)
	req.content_type = "application/json"
	http = Net::HTTP.new(uri.hostname, uri.port)
	req.body = batch.to_json
	result = http.request(req)
	STDERR.puts "result 1: #{result}" if @@debug
	uri = URI.parse("#{@@solr}update?commit=true") if @@debug
	req = Net::HTTP::Post.new(uri)
	http = Net::HTTP.new(uri.hostname, uri.port)
	result = http.request(req)
	STDERR.puts "result 2: #{result}" if @@debug
    end

    def Persons.location= location
	@@location = location
    end

    def Persons.solr= solr
	@@solr = solr
    end

    def Persons.debug= debug
	@@debug = debug
    end

    def Persons.debug
	@@debug
    end

    def Persons.size
	@@persons.size
    end

    def Persons.all_persons
	@@persons
    end

    def Persons.find id
	@@persons[id]
    end
end

