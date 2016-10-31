## Author: Mo McRoberts <mo.mcroberts@bbc.co.uk>
##
## Copyright (c) 2014 BBC
##
##  Licensed under the Apache License, Version 2.0 (the "License");
##  you may not use this file except in compliance with the License.
##  You may obtain a copy of the License at
##
##      http://www.apache.org/licenses/LICENSE-2.0
##
##  Unless required by applicable law or agreed to in writing, software
##  distributed under the License is distributed on an "AS IS" BASIS,
##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
##  See the License for the specific language governing permissions and
##  limitations under the License.

DB5FILES = \
	nav-sample.xml links-sample.xml res-links.xml res-nav.xml local.css \
	docbook-html5.xml \
	docbook-html5.xsl doc.xsl block.xsl inline.xsl toc.xsl

EXTRA_DIST = README.md LICENSE-2.0 $(DB5FILES)

if INSTALL_DOCBOOK_HTML5

dbh5htdoc_DATA = $(DB5FILES) index.html

index.html: $(srcdir)/docbook-html5.xml $(XSLTFILES) $(NAV) $(LINKS)
	$(XML2HTML) $<

endif