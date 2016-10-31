This is a set of XSL stylesheets for transforming DocBook 5 XML into (X)HTML5,
developed as part of the Research and Education Space project in order to
produce specific mark-up from our DocBook 5 sources.

They have no direct connection to the extremely comprehensive official
[DocBook XSL stylesheets](http://docbook.sourceforge.net/release/xsl/1.76.1/doc/), and don't have the same level of element coverage, but you may find them
easier to modify than the official XSL package.

To use remotely:

`xsltproc --xinclude http://bbcarchdev.github.io/docbook-html5/docbook-html5.xsl source.xml > dest.html`

Or, if you have a local copy (for example, as a git submodule):

`xsltproc --nonet --xinclude /path/to/docbook-html5.xsl source.xml > dest.html`

There are currently two parameters supported:

`html.linksfile`: specifies the (full) path to an XML file whose root element
is `include`, and whose contents will be substituted into the HTML `head`
element, primarily in order to include links to CSS stylesheets in the
output.

`html.navfile`: specifies the (full) path to an XML file whose root element
is `include`, and whose contents will be substituted into a `nav` element
within the output document's `header`. Typically, the contents are an
un-ordered list making up global navigation elements.

Note that `xsltproc` will interpret `html.linksfile` and `html.navfile` as
URLs relative to the stylesheet path, and so you may need to specify a `file://`
prefix to a local path.

With both parameters, a full processing command might be:

`xsltproc --xinclude  --param html.linksfile "'file:///path/to/links.xml'" --param html.navfile "'file:///path/to/nav.xml'" /path/or/url/to/docbook-html5.xsl source.xml > dest.html`

(Note that double-quoting is used to ensure that the actual quotation marks
are passed to `xsltproc` to indicate that the parameter value is a string).
