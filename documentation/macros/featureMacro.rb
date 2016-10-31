require 'asciidoctor'
require 'asciidoctor/extensions'
require 'ap'
require 'pp'

class TodoInlineMacro < Asciidoctor::Extensions::InlineMacroProcessor
  use_dsl
  
  named :todo
  name_positional_attributes 'description'
  
  def process parent, target, attrs
    %(<tim:todo xmlns:tim="http://timbuctoo.huygens.knaw.nl/docbook-extensions" type="#{target}">#{attrs['description']}</tim:todo>)
  end
end

class GlossaryMacro < Asciidoctor::Extensions::InlineMacroProcessor
  use_dsl
  
  named :gloss
  name_positional_attributes 'keyTerm'
  
  def process parent, target, attrs
    keyterm = attrs["keyTerm"] || target
    %(<tim:gloss xmlns:tim="http://timbuctoo.huygens.knaw.nl/docbook-extensions" term="#{keyterm}">#{target}</tim:gloss>)
  end
end

Asciidoctor::Extensions.register do
  inline_macro TodoInlineMacro
  inline_macro GlossaryMacro
end
