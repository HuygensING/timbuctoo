class DocumentReceptions

    @@wanted_document_receptions = Array.new

    def DocumentReceptions.add wanted_reception
	@@wanted_document_receptions << wanted_reception
    end

    def DocumentReceptions.get_wanted
	@@wanted_document_receptions
    end
end

