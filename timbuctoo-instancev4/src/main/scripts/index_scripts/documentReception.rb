class DocumentReception < Hash

    def initialize wanted_reception
	reception_doc_id = wanted_reception['reception_id']
	recepted_doc_id = wanted_reception['document_id']
	relation_id = wanted_reception['relation_id']
	relation_type = wanted_reception['relationType']

	reception_doc = Documents.find reception_doc_id
	if reception_doc.nil?
	    STDERR.puts "reception_doc: #{reception_doc_id} NOT FOUND!"
	    return
	end
	recepted_doc = Documents.find recepted_doc_id
	if recepted_doc.nil?
	    STDERR.puts "recepted_doc: #{recepted_doc_id} NOT FOUND!"
	    return
	end

	Documents.count_doc_rels_inc

#	new_rr = Hash.new
	self['type_s'] = "document_reception"
	self['id'] = relation_id
	self['reception_id_s'] = reception_doc_id
	self['relationType_s'] = relation_type
	self['displayName_s'] = reception_doc['displayName']
	self['title_t'] = reception_doc["title"]
	self['date_i'] = reception_doc["date_i"]
	self['notes_t'] = reception_doc["notes_t"]
	self["documentType_s"] = reception_doc["documentType_s"]
	Document.new_rel_names.each do |name|
	    self["document_#{name}"] = reception_doc[name]
	end

	self['document_id_s'] = recepted_doc_id
	self['document_displayName_s'] = recepted_doc['displayName']
	self['document_documentType_s'] = recepted_doc['documentType_s']
	self['document_date_i'] = recepted_doc['date_i']
	self['document_notes_t'] = recepted_doc['notes_t']
	self['document_title_t'] = recepted_doc['title_t']
	Document.new_rel_names.each do |name|
	    self["document_#{name}"] = recepted_doc[name]
	end
	self['_childDocuments_'] = recepted_doc['_childDocuments_']

#	Documents.complete_document_receptions_add new_rr
    end

end

