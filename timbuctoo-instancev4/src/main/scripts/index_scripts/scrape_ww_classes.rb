require 'open-uri'
require 'pp'
require './timer.rb'
require './person.rb'
require './persons.rb'
require './document.rb'
require './documents.rb'
require './documentreception.rb'
require './documentreceptions.rb'
require 'json'


if __FILE__ == $0
    Timer.start

    debug = false
    output_dir = ""
    multiple_archives = ""
    @location = "http://test.repository.huygens.knaw.nl/"
    @person_coll = "wwpersons/"
    @document_coll = "wwdocuments/"
    @pers_reception_coll = "wwpersonreceptions/"
    @doc_reception_coll = "wwdocumentreceptions/"
#    @collection = "gettingstarted/"
    @solr = "http://192.168.99.100:8983/solr/"

    begin
	(0..(ARGV.size-1)).each do |i|
	case ARGV[i]
	    when '--debug'
		debug = true
#	    when '-coll'
#		@collection = ARGV[i+1]
	    when '-loc'
		@location = ARGV[i+1]
	    when '-solr'
		@solr = "#{ARGV[i+1]}"
	    when '-h'
		STDERR.puts "use: ruby scrape_ww_classes.rb -loc location -solr solr-site [--debug]"
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
    Persons.solr = "#{@solr}#{@person_coll}"
    Persons.debug = false
    Document.location = "#{@location}v2.1/"
    Documents.location = "#{@location}v2.1/"
    Documents.solr_documents = "#{@solr}#{@document_coll}"
    Documents.solr_receptions = "#{@solr}#{@pers_reception_coll}"
    Documents.solr_doc_receptions = "#{@solr}#{@doc_reception_coll}"
    Documents.debug = debug

    continu = true
    start_value = 0
    num_of_lines = 100 # Persons.debug ? 10 : 100
    STDERR.puts "scrape persons"
    while(continu)
	continu = Persons.scrape_file start_value,num_of_lines
#	continu = false if Persons.debug && start_value==200
	start_value += 100 if continu
    end
    STDERR.puts

    puts "number of persons: #{Persons.size}"

    continu = true
    start_value = 0
    num_of_lines = 100 # debug ? 10 : 100
    STDERR.puts "scrape documents"
    while(continu)
	continu = Documents.scrape_file start_value,num_of_lines
#	continu = false if debug && start_value==200
	start_value += 100 if continu
    end
    STDERR.puts

    Documents.solr_commit "#{@solr}#{@document_coll}"
    Documents.solr_commit "#{@solr}#{@pers_reception_coll}"

    STDERR.puts "start met document receptions" if debug
    puts "#{DocumentReceptions.get_wanted.size} document receptions waiting to be found and committed"
    STDERR.puts "#{DocumentReceptions.get_wanted.size} document receptions waiting to be found and committed"
    #

    document_receptions = Array.new
    count_reception = 0
    DocumentReceptions.get_wanted.each do |doc_rec_data|
	document_receptions << DocumentReception.new(doc_rec_data)
	count_reception += 1
	if document_receptions.size >= 100
	    Documents.do_solr_update(document_receptions,
		    Documents.solr_doc_receptions)
	    document_receptions = Array.new
	end
    end
    if document_receptions.size > 0
	Documents.do_solr_update(document_receptions,
	    Documents.solr_doc_receptions)
    end
    #  

    Documents.solr_commit Documents.solr_doc_receptions

    puts "number of documents: #{Documents.number}"
    puts "number of document receptions comitted: #{count_reception}"

    Timer.stop
end

