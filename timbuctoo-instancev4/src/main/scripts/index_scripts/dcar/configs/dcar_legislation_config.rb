class DcarLegislationConfig
  def DcarLegislationConfig.get
      {
        :properties => [
            { :name => '_id',  :converted_name => 'id'},
            { :name => '@type',  :converted_name => 'type_s'},
            { :name => 'titleEng', :converted_name => 'titleEng_t'},
            { :name => 'titleNld', :converted_name => 'titleNld_t'},
            { :name => 'contents', :converted_name => 'contents_t'},
        ], :relations => [
            { :relation_name => 'has_legislation_keyword', :property_name => 'displayName', :converted_name => 'subject_ss' },
            { :relation_name => 'has_legislation_place', :property_name => 'displayName', :converted_name => 'place_ss' },
            { :relation_name => 'has_legislation_person', :property_name => 'displayName', :converted_name => 'person_ss' },
            { :relation_name => 'hasDeathPlace', :property_name => 'displayName', :converted_name => 'deathPlace_ss' },
            { :relation_name => 'hasMaritalStatus', :property_name => 'displayName', :converted_name => 'maritalStatus_ss' },
            { :relation_name => 'hasSocialClass', :property_name => 'displayName', :converted_name => 'socialClass_ss' },
            { :relation_name => 'hasEducation', :property_name => 'displayName', :converted_name => 'education_ss' },
            { :relation_name => 'hasReligion', :property_name => 'displayName', :converted_name => 'religion_ss' },
            { :relation_name => 'hasProfession', :property_name => 'displayName', :converted_name => 'profession_ss' },
            { :relation_name => 'hasFinancialSituation', :property_name => 'displayName', :converted_name => 'financialSituation_ss' },
            { :relation_name => 'isMemberOf', :property_name => 'displayName', :converted_name => 'memberships_ss' },

            # Locates publications
            { :relation_name => 'isCreatorOf', :property_name => 'id', :converted_name => '@workIds'}
        ]
      }
    end
end
