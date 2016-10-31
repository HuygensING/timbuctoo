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

	<!-- Generate the table of contents -->
	<xsl:template name="html.toc">
		<xsl:choose>
			<xsl:when test="/node()/db:toc">
				<xsl:call-template name="html.explicittoc" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="html.autotoc" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- Emit a provided table of contents -->
	<xsl:template name="html.explicittoc">
		<nav class="toc">
			<h1>Table of contents</h1>
			<ol>
				<xsl:apply-templates select="/node()/db:toc/*" mode="body" />
			</ol>
		</nav>
	</xsl:template>		
	
	<xsl:template match="//db:tocentry" mode="body">
		<xsl:param name="role" select="@role" />		
		<li>
			<xsl:if test="normalize-space($role) != ''"><xsl:attribute name="class"><xsl:value-of select="$role" /></xsl:attribute></xsl:if> 
			<xsl:apply-templates select="node()" mode="body" />
		</li>
	</xsl:template>

	<xsl:template match="//db:tocdiv" mode="body">
		<xsl:param name="role" select="@role" />		
		<xsl:param name="title" select="db:title" />
		<xsl:choose>
			<xsl:when test="parent::db:toc">
					<li>
						<xsl:if test="normalize-space($role) != ''"><xsl:attribute name="class"><xsl:value-of select="$role" /></xsl:attribute></xsl:if>
						<xsl:value-of select="$title" />
						<ol>
							<xsl:apply-templates select="*" mode="body" />
						</ol>
					</li>
			</xsl:when>
			<xsl:otherwise>		
				<xsl:value-of select="$title" />
				<ol>
					<xsl:apply-templates select="*" mode="body" />
				</ol>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- Automatically generate a table of contents when db:book is the root -->
	<xsl:template name="html.autotoc">
		<xsl:for-each select="/db:book">
			<xsl:choose>
				<xsl:when test="db:preface|db:acknowledgements|db:section|db:chapter|db:part|db:refsection|db:refsect1|db:refsect2|db:refsect3|db:refsect4|db:reference|db:refsynopsisdiv|db:refentry|db:glossary|db:bibliography|db:index|db:colophon">
					<nav class="toc">
						<h1>Table of contents</h1>
						<ol>
							<xsl:for-each select="db:preface|db:acknowledgements">
								<xsl:call-template name="html.toc.entry">
									<xsl:with-param name="class" select="'frontmatter'" />
								</xsl:call-template>
							</xsl:for-each>
							<xsl:for-each select="db:section|db:chapter|db:part|db:refsection|db:refsect1|db:refsect2|db:refsect3|db:refsect4|db:reference|db:refsynopsisdiv|db:refentry">
								<xsl:call-template name="html.toc.entry" />
							</xsl:for-each>
							<xsl:for-each select="db:glossary|db:bibliography|db:index|db:colophon|db:appendix">
								<xsl:call-template name="html.toc.entry">
									<xsl:with-param name="class" select="'backmatter'" />
								</xsl:call-template>
							</xsl:for-each>
							
						</ol>			
					</nav>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="html.toc.list">
		<xsl:param name="class" />	
		<xsl:choose>
			<xsl:when test="db:section|db:chapter|db:part|db:refsection|db:refsect1|db:refsect2|db:refsect3|db:refsect4|db:reference|db:refsynopsisdiv|db:refentry">
				<ol>
					<xsl:for-each select="db:section|db:chapter|db:part|db:refsection|db:refsect1|db:refsect2|db:refsect3|db:refsect4|db:reference|db:refsynopsisdiv|db:refentry">
						<xsl:call-template name="html.toc.entry">
							<xsl:with-param name="class" select="$class" />
						</xsl:call-template>
					</xsl:for-each>
				</ol>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="*">
					<xsl:call-template name="html.toc.list">
						<xsl:with-param name="class" select="$class" />
					</xsl:call-template>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="html.toc.entry">
		<xsl:param name="class" />	
		<xsl:variable name="title">
			<xsl:choose>
				<xsl:when test="db:title">
					<xsl:copy-of select="db:title" />
				</xsl:when>
				<xsl:when test="db:refmeta/db:refentrytitle">
					<xsl:copy-of select="db:refmeta/db:refentrytitle" />
				</xsl:when>
				<xsl:when test="local-name() = 'preface'">
					<xsl:text>Preface</xsl:text>
				</xsl:when>
				<xsl:when test="local-name() = 'acknowledgements'">
					<xsl:text>Acknowledgements</xsl:text>
				</xsl:when>
				<xsl:when test="local-name() = 'index'">
					<xsl:text>Index</xsl:text>
				</xsl:when>
				<xsl:when test="local-name() = 'glossary'">
					<xsl:text>Glossary</xsl:text>
				</xsl:when>
				<xsl:when test="local-name() = 'bibliography'">
					<xsl:text>Bibliography</xsl:text>
				</xsl:when>
				<xsl:when test="local-name() = 'colophon'">
					<xsl:text>Colophon</xsl:text>
				</xsl:when>				
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="id"><xsl:copy-of select="normalize-space(@xml:id)" /></xsl:variable>
		<xsl:choose>
			<xsl:when test="normalize-space($title) != ''">
				<li>
					<xsl:if test="normalize-space($class) != ''">
						<xsl:attribute name="class">
							<xsl:value-of select="$class" />
						</xsl:attribute>
					</xsl:if>
					<xsl:choose>
						<xsl:when test="normalize-space($id) != ''">
							<a>
								<xsl:attribute name="href"><xsl:value-of select="concat('#', $id)" /></xsl:attribute>
								<xsl:value-of select="$title" />
							</a>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$title" />
						</xsl:otherwise>						
					</xsl:choose>
					<xsl:call-template name="html.toc.list">
						<xsl:with-param name="class" select="$class" />
					</xsl:call-template>						
				</li>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="*">
					<xsl:call-template name="html.toc.list">
						<xsl:with-param name="class" select="$class" />
					</xsl:call-template>					
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>