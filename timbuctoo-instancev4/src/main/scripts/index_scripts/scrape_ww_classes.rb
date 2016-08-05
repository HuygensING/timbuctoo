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

    puts "number of documents: #{Documents.number}"
    puts "number of document receptions: #{Documents.document_receptions.size}"

    doc_recptions = Array.new
    STDERR.puts "start met document receptions" if debug
    Documents.document_receptions.each_with_index do |dr,ind|
	doc = Documents.find dr['document_id_s']
	# voeg doc eigenschappen toe aan reception
	    		    # hier alle data toevoegen
	new_dr = dr
	new_dr['document_documentType_s'] = doc['documentType_s']
	new_dr['document_date_i'] = doc['date_i']
	new_dr['document_notes_t'] = doc['notes_t']
	Document.new_rel_names.each do |name|
	    new_dr["document_#{name}"] = doc[name]
	end
	new_dr['_childDocuments_'] = doc['_childDocuments_']
	doc_recptions << new_dr
	if doc_recptions.size == 100
#	    STDERR.puts doc_recptions.last
	    Documents.do_solr_update doc_recptions,Documents.solr_doc_receptions
	    doc_recptions = Array.new
	end
    end
    if doc_recptions.size > 0
	STDERR.puts doc_recptions.last if debug
	Documents.do_solr_update doc_recptions,Documents.solr_doc_receptions
    end
    Documents.solr_commit "#{Documents.solr_doc_receptions}"

    Timer.stop
end

