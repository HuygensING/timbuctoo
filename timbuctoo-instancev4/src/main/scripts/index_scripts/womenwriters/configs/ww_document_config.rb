class WwDocumentConfig

  def WwDocumentConfig.get
    {
      :properties => [
        { :name => '_id',  :converted_name => 'id'},
        { :name => 'title', :converted_name => 'displayName_s'},
        { :name => 'title', :converted_name => 'title_t'},
        { :name => 'documentType', :converted_name => 'documentType_s'},
        { :name => 'englishTitle', :converted_name  => '^englishTitle'},
        { :name => 'date', :converted_name => 'date_i', :type => 'year'},
        { :name => 'notes', :converted_name => 'notes_t'},
        { :name => ['^modified', 'timeStamp'], :converted_name => 'modified_l' },
        { :name => ['^modified', 'userId'], :converted_name => 'modifiedBy_s' },
        { :name => ['^modified', 'username'], :converted_name => 'modifiedBy_s' },
      ], :relations => [
        { :relation_name => 'hasPublishLocation', :property_name => 'displayName', :converted_name => 'publishLocation_ss'},
        { :relation_name => 'hasWorkLanguage', :property_name => 'displayName', :converted_name => 'language_ss'},
        { :relation_name => 'hasGenre', :property_name => 'displayName', :converted_name => 'genre_ss'},
        { :relation_name => 'hasDocumentSource', :property_name => 'displayName', :converted_name => 'source_ss'},

        # Locates reception sources
        { :relation_name => "isBiographyOf", :property_name => "id", :converted_name => "isBiographyOf" },
        { :relation_name => "commentsOnPerson", :property_name => "id", :converted_name => "commentsOnPerson" },
        { :relation_name => "isDedicatedTo", :property_name => "id", :converted_name => "isDedicatedTo" },
        { :relation_name => "isAwardForPerson", :property_name => "id", :converted_name => "isAwardForPerson" },
        { :relation_name => "listsPerson", :property_name => "id", :converted_name => "listsPerson" },
        { :relation_name => "mentionsPerson", :property_name => "id", :converted_name => "mentionsPerson" },
        { :relation_name => "isObituaryOf", :property_name => "id", :converted_name => "isObituaryOf" },
        { :relation_name => "quotesPerson", :property_name => "id", :converted_name => "quotesPerson" },
        { :relation_name => "referencesPerson", :property_name => "id", :converted_name => "referencesPerson" },
        { :relation_name => "isEditionOf", :property_name => "id", :converted_name => "isEditionOf" },
        { :relation_name => "isSequelOf", :property_name => "id", :converted_name => "isSequelOf" },
        { :relation_name => "isTranslationOf", :property_name => "id", :converted_name => "isTranslationOf" },
        { :relation_name => "isAdaptationOf", :property_name => "id", :converted_name => "isAdaptationOf" },
        { :relation_name => "isPlagiarismOf", :property_name => "id", :converted_name => "isPlagiarismOf" },
        { :relation_name => "hasAnnotationsOn", :property_name => "id", :converted_name => "hasAnnotationsOn" },
        { :relation_name => "isBibliographyOf", :property_name => "id", :converted_name => "isBibliographyOf" },
        { :relation_name => "isCensoringOf", :property_name => "id", :converted_name => "isCensoringOf" },
        { :relation_name => "commentsOnWork", :property_name => "id", :converted_name => "commentsOnWork" },
        { :relation_name => "isAnthologyContaining", :property_name => "id", :converted_name => "isAnthologyContaining" },
        { :relation_name => "isCopyOf", :property_name => "id", :converted_name => "isCopyOf" },
        { :relation_name => "isAwardForWork", :property_name => "id", :converted_name => "isAwardForWork" },
        { :relation_name => "isPrefaceOf", :property_name => "id", :converted_name => "isPrefaceOf" },
        { :relation_name => "isIntertextualTo", :property_name => "id", :converted_name => "isIntertextualTo" },
        { :relation_name => "listsWork", :property_name => "id", :converted_name => "listsWork" },
        { :relation_name => "mentionsWork", :property_name => "id", :converted_name => "mentionsWork" },
        { :relation_name => "isParodyOf", :property_name => "id", :converted_name => "isParodyOf" },
        { :relation_name => "quotesWork", :property_name => "id", :converted_name => "quotesWork" },
        { :relation_name => "referencesWork", :property_name => "id", :converted_name => "referencesWork" },

        # Locates creators
        { :relation_name => 'isCreatedBy', :property_name => 'id', :converted_name => '@authorIds'}
      ]
    }
  end
end