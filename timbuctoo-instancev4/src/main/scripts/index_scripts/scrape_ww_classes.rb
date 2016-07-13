require 'open-uri'
require 'pp'
require './timer.rb'
require './person.rb'
require './persons.rb'
require './document.rb'
require './documents.rb'
require 'json'


if __FILE__ == $0
    Timer.start

    debug = false
    output_dir = ""
    multiple_archives = ""
    @location = "http://test.repository.huygens.knaw.nl/"
    @collection = "gettingstarted/"
    @solr = "http://192.168.99.100:8983/solr/#{@collection}"

    begin
	(0..(ARGV.size-1)).each do |i|
	case ARGV[i]
	    when '--debug'
		debug = true
	    when '-coll'
		@collection = ARGV[i+1]
	    when '-loc'
		@location = ARGV[i+1]
	    when '-solr'
		@solr = "#{ARGV[i+1]}#{@collection}"
	    when '-h'
		STDERR.puts "use: ruby scrape_ww_classes.rb -coll collection -loc location -solr solr-site [--debug]"
		exit(1)
	end
    end
    rescue => detail
	STDERR.puts "#{detail}"
    end

    #
    # scrape persons en bewaar in Hash
    # scrape documents en gebruik de person-hash
    #

    Person.location = "#{@location}v2.1/"
    Persons.location = "#{@location}v2.1/"
    Persons.solr = @solr
    Persons.debug = false
    Document.location = "#{@location}v2.1/"
    Documents.location = "#{@location}v2.1/"
    Documents.solr = @solr
    Documents.debug = debug


    continu = true
    start_value = 0
    num_of_lines = Persons.debug ? 10 : 100
    while(continu)
	continu = Persons.scrape_file start_value,num_of_lines
	continu = false if Persons.debug && start_value==200
	start_value += 100 if continu
    end

    puts "number of persons: #{Persons.size}"

    continu = true
    start_value = 0
    num_of_lines = debug ? 10 : 100
    while(continu)
	continu = Documents.scrape_file start_value,num_of_lines
	continu = false if debug && start_value==200
	start_value += 100 if continu
    end

    puts "number of documents: #{Documents.number}"

    Timer.stop
end

