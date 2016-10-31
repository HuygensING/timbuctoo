<?xml version="1.0" encoding="utf-8"?>
<!--

 Author: Mo McRoberts <mo.mcroberts@bbc.co.uk>

 Copyright (c) 2014 BBC

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:db="http://docbook.org/ns/docbook" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xlink="http://www.w3.org/1999/xlink">
	
	<!-- Bibliography inlines -->

	<!-- <citation> -->
	<xsl:template match="//db:citation" mode="body">
		<xsl:text>[</xsl:text>
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">cite</xsl:with-param>
			<xsl:with-param name="kind" />
		</xsl:call-template>
		<xsl:text>]</xsl:text>
	</xsl:template>
	
	<!-- <citetitle> -->
	<xsl:template match="//db:citetitle" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">cite</xsl:with-param>
			<xsl:with-param name="kind" />
		</xsl:call-template>
	</xsl:template>
	
	<!-- Error inlines -->
	
	<!-- Graphic inlines -->
	
	<!-- GUI inlines -->
	
	<!-- <menuchoice> -->
	<xsl:template match="//db:menuchoice" mode="body">
		<xsl:element name="kbd">
			<xsl:call-template name="html.inline.attrs" />
			<xsl:for-each select="node()">
				<xsl:choose>
					<xsl:when test="namespace-uri() = 'http://docbook.org/ns' and local-name() = 'shortcut'" />
					<xsl:when test="self::*">
						<xsl:apply-templates select="." mode="body" />
						<xsl:if test="position() != last()">
							<xsl:text> → </xsl:text>
						</xsl:if>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates mode="body" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			<xsl:for-each select="//db:shortcut">
				<xsl:element name="samp">
					<xsl:attribute name="class">shortcut</xsl:attribute>
					<xsl:text> (</xsl:text>
					<xsl:apply-templates select="node()" mode="body" />
					<xsl:text>)</xsl:text>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>

	<!-- <guimenu>, <guimenuitem>, <guisubmenu>, <guibutton>, <guiicon>, <guilabel>, <mousebutton> -->
	<xsl:template match="//db:guimenu|//db:guimenuitem|//db:guisubmenu|//db:guibutton|//db:guiicon|//db:guilabel|//db:mousebutton" mode="body">
		<xsl:element name="kbd">
			<xsl:call-template name="html.inline.attrs" />
			<xsl:element name="samp">
				<xsl:apply-templates select="node()" mode="body" />
			</xsl:element>
		</xsl:element>
	</xsl:template>
		
	<!-- Indexing inlines -->
	
	<!-- Keyboard inlines -->
	
	<!-- Linking inlines -->
	
	<!-- <anchor> -->
	<xsl:template match="//db:anchor" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">a</xsl:with-param>
			<xsl:with-param name="kind" />
		</xsl:call-template>
	</xsl:template>
	
	<!-- <link> -->
	<xsl:template match="//db:link" mode="body">
		<xsl:element name="a">
			<xsl:attribute name="href"><xsl:value-of select="@xlink:href" /></xsl:attribute>
			<xsl:call-template name="html.inline.attrs"><xsl:with-param name="kind" /></xsl:call-template>
			<xsl:apply-templates select="node()" mode="body" />
		</xsl:element>
	</xsl:template>

	<!-- <olink> -->

	<!-- <xref> -->
	
	<!-- <biblioref -->
	
	<!-- Markup inlines -->

	<!-- <code> -->
	<xsl:template match="//db:code" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">code</xsl:with-param>
			<xsl:with-param name="kind" />
		</xsl:call-template>
	</xsl:template>
	
	<!-- <markup>, <constant>, <symbol>, <token>, <literal>, <uri>, <cmdsynopsis> -->
	<xsl:template match="//db:markup|//db:constant|//db:symbol|//db:token|//db:literal|//db:uri" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">code</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- <email> -->
	<xsl:template match="//db:email" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">span</xsl:with-param>
		</xsl:call-template>
	</xsl:template>	
	
	<!-- <tag> -->
	<xsl:template match="//db:tag" mode="body">
		<xsl:choose>
			<xsl:when test="attribute::class='element' or attribute::class='starttag'" mode="body">
				<xsl:call-template name="html.inline">
		  			<xsl:with-param name="element">code</xsl:with-param>
					<xsl:with-param name="prefix">&lt;</xsl:with-param>
					<xsl:with-param name="suffix">&gt;</xsl:with-param>
		  		</xsl:call-template>
			</xsl:when>
			<xsl:when test="attribute::class='emptytag'">
				<xsl:call-template name="html.inline">
		  			<xsl:with-param name="element">code</xsl:with-param>
					<xsl:with-param name="prefix">&lt;</xsl:with-param>
					<xsl:with-param name="suffix"> /&gt;</xsl:with-param>
		  		</xsl:call-template>
			</xsl:when>
			<xsl:when test="attribute::class='endtag'">
				<xsl:call-template name="html.inline">
		  			<xsl:with-param name="element">code</xsl:with-param>
					<xsl:with-param name="prefix">&lt;/</xsl:with-param>
					<xsl:with-param name="suffix">&gt;</xsl:with-param>
		  		</xsl:call-template>
			</xsl:when>
			<xsl:when test="attribute::class='genentity'">
				<xsl:call-template name="html.inline">
		  			<xsl:with-param name="element">code</xsl:with-param>
					<xsl:with-param name="prefix">&amp;</xsl:with-param>
					<xsl:with-param name="suffix">;</xsl:with-param>
		  		</xsl:call-template>
			</xsl:when>
			<xsl:when test="attribute::class='numcharref'">
				<xsl:call-template name="html.inline">
		  			<xsl:with-param name="element">code</xsl:with-param>
					<xsl:with-param name="prefix">&amp;#</xsl:with-param>
					<xsl:with-param name="suffix">;</xsl:with-param>
		  		</xsl:call-template>
			</xsl:when>
			<xsl:when test="attribute::class='comment'">
				<xsl:call-template name="html.inline">
		  			<xsl:with-param name="element">code</xsl:with-param>
					<xsl:with-param name="prefix">&lt;!--</xsl:with-param>
					<xsl:with-param name="suffix">--&gt;</xsl:with-param>
		  		</xsl:call-template>
			</xsl:when>
			<xsl:when test="attribute::class='xmlpi' or attribute::class='pi'">
				<xsl:call-template name="html.inline">
		  			<xsl:with-param name="element">code</xsl:with-param>
					<xsl:with-param name="prefix">&lt;?</xsl:with-param>
					<xsl:with-param name="suffix">&gt;</xsl:with-param>
		  		</xsl:call-template>
			</xsl:when>
			<xsl:when test="attribute::class='attvalue'">
				<xsl:call-template name="html.inline">
		  			<xsl:with-param name="element">code</xsl:with-param>
					<xsl:with-param name="prefix">"</xsl:with-param>
					<xsl:with-param name="suffix">"</xsl:with-param>
		  		</xsl:call-template>
			</xsl:when>			
			<xsl:otherwise>
				<xsl:call-template name="html.inline">
		  			<xsl:with-param name="element">code</xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- Math inlines -->
	
	<!-- Object-oriented programming inlines -->
	
	<!-- Operating system inlines -->
	
	<!-- <filename>, <command>, <envvar> -->
	<xsl:template match="//db:filename|//db:envvar" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">code</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- <command> -->
	<xsl:template match="//db:command" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">kbd</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
		
	<!-- <userinput> -->
	<xsl:template match="//db:userinput" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">kbd</xsl:with-param>
			<xsl:with-param name="kind" />
		</xsl:call-template>
	</xsl:template>
	
	<!-- <prompt> -->
	<xsl:template match="//db:prompt" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">samp</xsl:with-param>
			<xsl:with-param name="class">output</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- <computeroutput> -->
	<xsl:template match="//db:computeroutput" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">samp</xsl:with-param>
			<xsl:with-param name="kind">output</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- Product inlines -->
	
	<!-- <application>, <database>, <hardware>, <productname>, <productnumber>, <trademark> -->
	
	
	<!-- Programming inlines -->
	
	<!-- <classname>, <exceptionname>, <function>, <initializer>, <interfacename>,
	   - <methodname>, <modifier>, <ooclass>, <ooexception>, <oointerface>,
	   - <parameter>, <returnvalue>, <type>
	   -->
	<xsl:template match="//db:classname|//db:exceptionname|//db:function|//db:initializer|//db:interfacename|//db:methodname|//db:modifier|//db:ooclass|//db:ooexception|//db:oointerface|//db:parameter|//db:returnvalue|//db:type" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">code</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- <varname> -->
	<xsl:template match="//db:varname" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">var</xsl:with-param>
			<xsl:with-param name="kind" />
		</xsl:call-template>
	</xsl:template>
		
	
	<!-- Publishing inlines -->
	
	<!-- <abbrev> and <acronym> -->
	<xsl:template match="//db:abbrev|//db:acronym" mode="body">
		<xsl:variable name="title"><xsl:copy-of select="db:alt" /></xsl:variable>
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">abbr</xsl:with-param>
			<xsl:with-param name="kind" />
			<xsl:with-param name="title"><xsl:value-of select="$title" /></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- <emphasis> -->
	<xsl:template match="//db:emphasis" mode="body">
		<xsl:variable name="element">
			<xsl:choose>
				<xsl:when test="attribute::role='strong'">strong</xsl:when>
				<xsl:otherwise>em</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element" select="normalize-space($element)" />
			<xsl:with-param name="kind" />
		</xsl:call-template>
	</xsl:template>
	
	<!-- <phrase> -->
	<xsl:template match="//db:phrase" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">span</xsl:with-param>
			<xsl:with-param name="kind" />
		</xsl:call-template>
	</xsl:template>

	<!-- <foreignphrase>, <firstterm> -->
	<xsl:template match="//db:foreignphrase|//db:firstterm" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">i</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- Technical inlines -->	
	
	<!-- Ubiquitious inlines -->
	
	<!-- <remark> -->
	<xsl:template match="//db:remark" mode="body">
		<xsl:call-template name="html.inline">
			<xsl:with-param name="element">i</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- People -->
	
	<xsl:template match="//db:personname" mode="body">
		<xsl:if test="db:firstname|db:surname">			
			<xsl:if test="db:firstname">
				<xsl:apply-templates select="db:firstname" mode="body" />
				<xsl:if test="db:surname"><xsl:text> </xsl:text></xsl:if>
			</xsl:if>
			<xsl:if test="db:surname">
				<xsl:apply-templates select="db:surname" mode="body" />
			</xsl:if>
			<xsl:if test="db:affiliation">
				<xsl:text>, </xsl:text>
			</xsl:if>
		</xsl:if>
		<xsl:if test="db:affiliation">
			<xsl:if test="db:affiliation/db:org">
				<xsl:choose>
					<xsl:when test="db:affiliation/db:org/db:uri">
						<a>
							<xsl:attribute name="href"><xsl:value-of select="db:affiliation/db:org/db:uri" /></xsl:attribute>
							<xsl:apply-templates select="db:affiliation/db:org/db:orgname" mode="body" />
						</a>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="db:affiliation/db:org/db:orgname" mode="body" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	
	<!-- mediaobject -->
	<xsl:template match="//db:mediaobject" mode="body">
		<xsl:for-each select="node()">
			<xsl:choose>
				<xsl:when test="namespace-uri() = 'http://docbook.org/ns' and local-name() = 'imageobject'">
					<xsl:for-each select="db:imagedata">
						<img>
							<xsl:attribute name="src"><xsl:value-of select="@fileref" /></xsl:attribute>
						</img>
					</xsl:for-each>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<!-- Inline element output -->
	<xsl:template match="//db:alt|//db:manvolnum" mode="body"/>
	
	<xsl:template name="html.inline">
		<xsl:param name="element" select="'span'" />
		<xsl:param name="class" />
		<xsl:param name="role" select="@role" />
		<xsl:param name="id" select="normalize-space(@xml:id)" />		
		<xsl:param name="kind" select="local-name()" />
		<xsl:param name="prefix" />
		<xsl:param name="suffix" />
		<xsl:param name="title" />
		<xsl:element name="{$element}">
			<xsl:call-template name="html.inline.attrs">
				<xsl:with-param name="class" select="$class" />
				<xsl:with-param name="role" select="$role" />
				<xsl:with-param name="id" select="$id" />
				<xsl:with-param name="kind" select="$kind" />
				<xsl:with-param name="title" select="$title" />
			</xsl:call-template>
			<xsl:copy-of select="$prefix" /><xsl:apply-templates select="node()" mode="body" /><xsl:copy-of select="$suffix" />
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="html.inline.attrs">
		<xsl:param name="class" />
		<xsl:param name="role" select="@role" />
		<xsl:param name="id" select="normalize-space(@xml:id)" />		
		<xsl:param name="kind" select="local-name()" />
		<xsl:param name="title" />
		<xsl:variable name="classes" select="normalize-space(concat($class, ' ', $role, ' ', $kind))" />
		<xsl:if test="$id != ''"><xsl:attribute name="id"><xsl:value-of select="$id" /></xsl:attribute></xsl:if>			
		<xsl:if test="$classes != ''"><xsl:attribute name="class"><xsl:value-of select="$classes" /></xsl:attribute></xsl:if> 
		<xsl:if test="normalize-space($title) != ''"><xsl:attribute name="title"><xsl:value-of select="$title" /></xsl:attribute></xsl:if>
	</xsl:template>	

</xsl:stylesheet>