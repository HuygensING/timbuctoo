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
<xsl:stylesheet xmlns:tim="http://timbuctoo.huygens.knaw.nl/docbook-extensions" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:db="http://docbook.org/ns/docbook" xmlns:xi="http://www.w3.org/2001/XInclude">

	<!-- Root element switch -->
	<xsl:template match="/">
		<xsl:variable name="doc.title">
			<xsl:value-of select="/db:article/db:info/db:title" />
		</xsl:variable>
		<xsl:variable name="doc.subtitle">
			<xsl:value-of select="/db:article/db:info/db:subtitle" />
		</xsl:variable>
		<xsl:call-template name="html.doctype" />
		<html lang="en">
			<head>
				<xsl:call-template name="html.meta" />
				<title><xsl:value-of select="$doc.title" /></title>
				<link rel="stylesheet" href="./bootstrap.min.css" />
				<!--<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous" />-->
				<!--<link href="http://timbuctoo.huygens.knaw.nl/wp-content/themes/HI-it/css/bootstrap.css" rel="stylesheet" />
				<link href="http://timbuctoo.huygens.knaw.nl/wp-content/themes/HI-it/css/hi-style.css" rel="stylesheet" type="text/css" />-->
				<script type="text/javascript" src="./jquery.js">/**/</script>
				<script src="./bootstrap.min.js">/**/</script>
				<!--<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.1/jquery.js" integrity="sha256-16cdPddA6VdVInumRGo6IbivbERE8p7CQR3HzTBuELA=" crossorigin="anonymous">/**/</script>
				<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous">/**/</script>-->
				<!--<meta http-equiv="refresh" content="2" />-->
				<style>
					/* body needs padding to make the top of the page visible when scrolled all the way to the top */ 
					body {
						padding-top: 70px;
					}
					/* section needs padding to make the top of the section visible when jumping there using an anchor tag */ 
					section {
						padding-top: 50px;
						margin-top: -50px;
					}
				</style>
			</head>
			<xsl:call-template name="html.body">
				<xsl:with-param name="kind">article</xsl:with-param>
				<xsl:with-param name="title" select="$doc.title" />
				<xsl:with-param name="subtitle" select="$doc.subtitle" />
			</xsl:call-template>
		</html>
	</xsl:template>
	
	<!-- Generate the HTML <body> -->
	<xsl:template name="html.body">
		<xsl:param name="kind" />
		<xsl:param name="title" />
		<xsl:param name="subtitle" />
		<body class="{$kind}">
			<xsl:call-template name="html.globalnav" />
			<div class="container">
				<xsl:call-template name="html.frontmatter">
					<xsl:with-param name="title" select="$title" />
					<xsl:with-param name="subtitle" select="$subtitle" />
				</xsl:call-template>
				<article>
					<xsl:apply-templates select="node()" mode="body" />
				</article>
				<xsl:call-template name="html.backmatter" />
			</div>
		</body>
	</xsl:template>
	
	<!-- Generate any frontmatter -->
	<xsl:template name="html.frontmatter">
		<xsl:param name="title" />
		<xsl:param name="subtitle" />
		<div class="jumbotron">
		<h1><xsl:value-of select="$title" /></h1>
		<xsl:if test="normalize-space($subtitle) != ''">
			<p><xsl:value-of select="$subtitle" /></p>
		</xsl:if>
		</div>
		<xsl:for-each select="db:info/db:edition"><p class="edition"><xsl:apply-templates select="node()" mode="body" /></p></xsl:for-each>
		<xsl:if test="db:info/db:editor">
			<p class="editor">
				<xsl:text>Edited by </xsl:text>
				<xsl:for-each select="db:info/db:editor">
					<xsl:if test="position() != 1"><xsl:text>, </xsl:text></xsl:if>
					<xsl:call-template name="html.person" />
				</xsl:for-each>
				<xsl:text>.</xsl:text>
			</p>
		</xsl:if>
		<!-- Use a series of xsl:for-each stanzas to output in a specific order -->
		<xsl:for-each select="db:info/db:legalnotice"><xsl:call-template name="html.titleblock" /></xsl:for-each>
		<xsl:for-each select="db:preface"><xsl:call-template name="html.titleblock" /></xsl:for-each>
		<xsl:for-each select="db:acknowledgements"><xsl:call-template name="html.titleblock" /></xsl:for-each>
		<xsl:call-template name="html.toc" />
	</xsl:template>
	
	<xsl:template name="html.globalnav">
		<nav class="navbar navbar-default navbar-fixed-top">
      <div class="container">
        <div class="navbar-header">
					<!-- the hamburger button for small screens-->
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"> </span>
            <span class="icon-bar"> </span>
            <span class="icon-bar"> </span>
          </button>
					<!-- the hamburger button for small screens-->
          <a class="navbar-brand" href="#">Timbuctoo</a>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
					<ul class="nav navbar-nav">
					<xsl:for-each select="/db:article/db:section">
						<xsl:call-template name="nav.item">
							<xsl:with-param name="depth">0</xsl:with-param>
						</xsl:call-template> 
					</xsl:for-each>
					</ul>
        </div>
      </div>
    </nav>
	</xsl:template>

	<xsl:template name="nav.item">
		<xsl:param name="depth" value="0" />
		<xsl:choose>
		<xsl:when test="./db:section">
			<li class="dropdown">
				<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
					<xsl:value-of select="./db:title"/> <span class="caret"></span>
				</a>
				<ul class="dropdown-menu">
					<xsl:for-each select="./db:section">
						<li>
							<a href="#">
								<xsl:attribute name="href">#<xsl:value-of select="./@xml:id" /></xsl:attribute>
								<xsl:value-of select="./db:title"/>
							</a>
						</li>
					</xsl:for-each>
				</ul>
			</li>
		</xsl:when>
		<xsl:otherwise>
			<li>
				<a href="#">
					<xsl:attribute name="href">#<xsl:value-of select="./@xml:id" /></xsl:attribute>
					<xsl:value-of select="./db:title"/>
				</a>
			</li>
		</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Emit a person's name -->
	<xsl:template name="html.person">
		<xsl:if test="db:personname">
			<xsl:apply-templates select="db:personname" mode="body" />
		</xsl:if>
	</xsl:template>
	
	<!-- Generate any backmatter -->
	<xsl:template name="html.backmatter">
		<!-- Use a series of xsl:for-each stanzas to output in a specific order -->
		<xsl:for-each select="db:appendix"><xsl:call-template name="html.titleblock" /></xsl:for-each>
		<xsl:for-each select="db:glossary"><xsl:call-template name="html.titleblock" /></xsl:for-each>
		<xsl:for-each select="db:bibliography"><xsl:call-template name="html.titleblock" /></xsl:for-each>
		<xsl:for-each select="db:index"><xsl:call-template name="html.titleblock" /></xsl:for-each>
		<xsl:for-each select="db:colophon"><xsl:call-template name="html.titleblock" /></xsl:for-each>
		<xsl:call-template name="html.todos" />
	</xsl:template>

	<xsl:template name="html.todos">
		<xsl:if test="//tim:feature">
			<h2>open todos</h2>
			<ul>
				<xsl:for-each select="//tim:feature">
					<li>
						<xsl:choose>
							<xsl:when test="@type = 'feature'">🎁 </xsl:when>
							<xsl:when test="@type = 'bug'">🐛 </xsl:when>
							<xsl:otherwise>?</xsl:otherwise>
						</xsl:choose>
						<xsl:value-of select="." />
					</li>
				</xsl:for-each>
			</ul>
		</xsl:if>
	</xsl:template>
		

	<!-- Don't emit certain elements -->
	<xsl:template match="//db:title" mode="body" />
	<xsl:template match="//db:subtitle" mode="body" />
	<xsl:template match="//db:info" mode="body" />
	<xsl:template match="//db:refmeta" mode="body" />
	<xsl:template match="//db:legalnotice|//db:preface|//db:acknowledgements|//db:toc|//db:dedication" mode="body" />
	<xsl:template match="//db:appendix|//db:bibliography|//db:colophon|//db:glossary|//db:index" mode="body" />
	
</xsl:stylesheet>
