class WwDocumentReceptionMapper
  def initialize(document_mapper)
    @document_mapper = document_mapper
  end

  def convert(reception_id: nil, document_id: nil, relation_id: nil, relationType: nil)
    received_doc = @document_mapper.find (document_id)
    reception = @document_mapper.find (reception_id)

    data = reception.dup

    data.delete("_childDocuments_")

    received_doc.keys.each do |key|
      data["document_#{key}"] = received_doc[key] unless key.eql?('id') or key.eql?("_childDocuments_")
    end

    data['id'] = relation_id
    data['type_s'] = 'document_reception'
    data['relationType_s'] = relationType
    data['reception_id_s'] = reception_id
    data['document_id_s'] = document_id
    data['_childDocuments_'] = received_doc['_childDocuments_']
    data
  end
end