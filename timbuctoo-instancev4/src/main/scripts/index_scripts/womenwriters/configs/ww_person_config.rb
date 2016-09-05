class WwPersonConfig
  def WwPersonConfig.get
      {
        :properties => [
            { :name => '_id',  :converted_name => 'id'},
            { :name => 'names', :converted_name => 'name_t', :type => 'names'},
            { :name => 'names', :converted_name => 'nameSort_s', :type => 'name_sort'},
            { :name => 'names', :converted_name => 'displayName_s', :type => 'names_display_name'},
            { :name => 'types', :converted_name => 'types_ss'},
            { :name => 'gender', :converted_name => 'gender_s'},
            { :name => 'birthDate', :converted_name => 'birthDate_i', :type => 'year'},
            { :name => 'deathDate', :converted_name => 'deathDate_i', :type => 'year'},
            { :name => 'notes', :converted_name => 'notes_t'},
            { :name => 'children', :converted_name => 'children_s'},
            { :name => ['^modified', 'timeStamp'], :converted_name => 'modified_l' },
            { :name => '@displayName', :converted_name => '@displayName'}
        ], :relations => [
            { :relation_name => ['hasResidenceLocation', 'hasBirthPlace', 'hasDeathPlace'], :property_name => 'displayName', :converted_name => 'relatedLocations_ss' },
            { :relation_name => 'hasBirthPlace', :property_name => 'displayName', :converted_name => 'birthPlace_ss' },
            { :relation_name => 'hasDeathPlace', :property_name => 'displayName', :converted_name => 'deathPlace_ss' },
            { :relation_name => 'hasMaritalStatus', :property_name => 'displayName', :converted_name => 'maritalStatus_ss' },
            { :relation_name => 'hasSocialClass', :property_name => 'displayName', :converted_name => 'socialClass_ss' },
            { :relation_name => 'hasEducation', :property_name => 'displayName', :converted_name => 'education_ss' },
            { :relation_name => 'hasReligion', :property_name => 'displayName', :converted_name => 'religion_ss' },
            { :relation_name => 'hasProfession', :property_name => 'displayName', :converted_name => 'profession_ss' },
            { :relation_name => 'hasFinancialSituation', :property_name => 'displayName', :converted_name => 'financialSituation_ss' },
            { :relation_name => 'isMemberOf', :property_name => 'displayName', :converted_name => 'memberships_ss' },


            { :relation_name => 'isCreatorOf', :property_name => 'id', :converted_name => '@workIds'}
        ]
      }
    end
end