class WwPersonReceptionMapper
  def initialize(person_mapper, document_mapper)
    @person_mapper = person_mapper
    @document_mapper = document_mapper
  end

  def convert(reception_id: nil, person_id: nil, relation_id: nil, relationType: nil)
    person = @person_mapper.find (person_id)
    reception = @document_mapper.find (reception_id)

    data = reception.dup

    data.delete("_childDocuments_")

    person.keys.each do |key|
      data["person_#{key}"] = person[key] unless key.eql?('id')
    end

    data['id'] = relation_id
    data['type_s'] = 'person_reception'
    data['relationType_s'] = relationType
    data['reception_id_s'] = reception_id
    data['person_id_s'] = person_id

    data
  end
end