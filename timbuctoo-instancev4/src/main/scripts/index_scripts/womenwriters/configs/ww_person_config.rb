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
        ]
      }
    end
end