<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

  <xsl:output method="xml" encoding="utf-8" media-type="application/xml" />

  <xsl:template match="/">
    <result>
      <xsl:apply-templates select="//lst[@name='termVectors']/lst/lst" />
    </result>
  </xsl:template>

  <xsl:template match="lst[@name='termVectors']/lst/lst">
    <field name="{@name}">
      <xsl:apply-templates select=".//int[@name='start']">
        <xsl:sort select="text()" data-type='number' />
      </xsl:apply-templates>
    </field>
  </xsl:template>

  <xsl:template match="int">
    <xsl:value-of select="parent::lst/parent::lst/@name" />
    <xsl:text> </xsl:text>
  </xsl:template>
  
</xsl:stylesheet>
