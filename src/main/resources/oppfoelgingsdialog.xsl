<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">
    <xsl:output method="xhtml" omit-xml-declaration="yes" encoding="UTF-8" indent="yes"/>
    <xsl:template match="/*:oppfoelgingsdialogXML">
        <xsl:variable name="sykmeldtnavn" select="sykmeldtnavn"/>
        <xsl:variable name="arbeidsgivernavn" select="arbeidsgivernavn"/>
        <html>
            <head>
                <meta charset="UTF-8"/>
                <style>
                    html {
                    font-size: 8pt;
                    }


                    .container {
                    display: block;
                    font-family: SourceSans, ArialSystem, Modus;
                    font-size: 10pt;
                    line-height: 1.4em;
                    margin: 0;
                    }

                    .headercontainer {
                    height: 60pt;
                    min-height: 60pt;
                    margin-bottom: 40pt;
                    }

                    .header {
                    margin-bottom: 20pt;
                    font-size: 24pt;
                    font-weight: 600;
                    margin-top: 0;
                    }

                    .headeringress {
                    font-size: 16pt;
                    margin-top: 0;
                    margin-bottom: 20pt;
                    }

                    .ingresstekst {
                    font-size: 12pt;
                    margin-top: 0;
                    margin-bottom: 8pt;
                    }

                    .ingress {
                    margin-bottom: 12pt;
                    }

                    .ikon {
                    margin-right: 8pt;
                    max-height: 16pt;
                    max-width: 16pt;
                    width: 16pt;
                    height: 16pt;
                    }

                    .ikon-tittel {
                    display: inline;
                    }

                    .varseltekst {
                    font-size: 10pt;
                    margin-bottom: 8pt;
                    }

                    .innholdoverskrift {
                    width: 100%;
                    height: 20pt;
                    border-bottom: 1pt solid #CFCFCF;
                    }

                    .innholdoverskrift--ingenborder {
                    border-bottom: none;
                    }

                    .innhold {
                    margin-top: 0;
                    margin-bottom: 32pt;
                    }

                    .innholdoverskrift {
                    margin-top: 0;
                    margin-bottom: 8pt;
                    padding-bottom: 4pt;
                    }

                    h2 {
                    font-size: 14pt;
                    font-weight: 600;
                    }

                    .hentetfra {
                    font-size: 10pt;
                    }

                    table {
                    table-layout: fixed;
                    width: 100%;
                    }

                    th {
                    font-size: 10pt;
                    font-weight: 600;
                    margin-right: 40pt;
                    }
                    td {
                    font-size: 10pt;
                    }

                    .left {
                    margin: 0;
                    float: left;
                    }

                    .fotnote {
                    margin-left: 40pt;
                    }

                    .right {
                    margin: 0;
                    float: right;
                    }

                    h3 {
                    font-size: 14pt;
                    font-weight: 600;
                    display: inline-block;
                    }

                    h4 {
                    font-size: 12pt;
                    margin-top: 8pt;
                    margin-bottom: 8pt;
                    font-weight: 600;
                    }

                    label {
                    display: block;
                    }

                    .blokk--l {
                    margin-top: 0;
                    margin-bottom: 16pt;
                    }

                    .bildeTittel {
                    margin-bottom: 16pt;
                    }

                    .sykeforloepsperioder {
                    page-break-inside: avoid;
                    }

                    .arbeidsoppgaver h3 {
                    margin-top: 0pt;
                    margin-bottom: 0pt;
                    }

                    .arbeidsoppgaver {
                    page-break-inside: avoid;
                    }

                    .arbeidsoppgaver_tilrettelegging {
                    margin-bottom: 8pt;
                    }

                    .innhold-tiltak {
                    page-break-inside: avoid;
                    }

                    .innhold-tiltak div:last-child {
                    margin-bottom: 0;
                    }

                    .panel--ok,
                    .panel--maybe,
                    .panel--not,
                    .panel--ingensvar {
                    page-break-inside: avoid;
                    margin-top: 0pt;
                    margin-bottom: 0pt;
                    word-wrap: break-word;
                    }

                    .panel--ok,
                    .panel--ingensvar {
                    margin-bottom: 8pt
                    }

                    .panel--tiltak {
                    page-break-inside: avoid;
                    margin-top: 2.3em;
                    margin-bottom: 6pt;
                    }

                    .kommentar {
                    font-size: 10pt;
                    margin-top: 0;
                    margin-bottom: 2pt;
                    }

                    .tiltak--dato {
                    font-size: 10pt;
                    margin-bottom: 2pt;
                    }

                    @page {
                    padding: 60pt 40pt;
                    margin-top: 60pt;
                    margin-left: 0;
                    margin-right: 0;
                    }

                    @page :first {
                    margin-top: 0;
                    }

                    #footer {
                    position: running(footer);
                    text-align: right;
                    }

                    @page {
                    @bottom-right {
                    content: element(footer);
                    }
                    }

                    #pagenumber:before {
                    content: counter(page);
                    }

                    #pagecount:before {
                    content: counter(pages);
                    }

                    .arbeidsoppgave-sitat-label {
                    font-weight: 600;
                    font-size: 8pt;
                    text-transform: uppercase;
                    line-height: 8pt;
                    }
                    .arbeidsoppgave-sitat {
                    line-height: 10pt;
                    display: block;
                    font-style: italic;
                    white-space: pre-line;
                    word-wrap: break-word;
                    }
                    .tiltak-sitat-label {
                    font-weight: 600;
                    font-size: 8pt;
                    text-transform: uppercase;
                    line-height: 8pt;
                    }

                    .tiltak-sitat {
                    line-height: 8pt;
                    display: block;
                    white-space: pre-line;
                    word-wrap: break-word;
                    }
                    .tiltak-opprettetAv {
                    margin-top: 8pt;
                    display: block;
                    white-space: pre-line;
                    }

                    .tiltak-italic {
                    font-style: italic;
                    }
                    .status {
                    width: 6em;
                    padding: 0.5em 1em 0.5em 1em;
                    margin-top: 1em;
                    margin-bottom: 1.5em;
                    text-align: center;
                    }

                    .tiltakGodkjent {
                    background-color: #9bd0b0;
                    }

                    .tiltakForslag {
                    background-color: #fdbc6d;
                    }

                    .tiltakIkkeAktuelt {
                    background-color: #e3b0a8;
                    }

                    .tiltak-info {
                    margin-bottom: 4pt;
                    }

                </style>
            </head>
            <body>
                <div class="container">
                    <div id="footer">
                        <div class="fotnote left"><xsl:value-of select="fotnote"/></div>
                        <div class="right">side <span id="pagenumber"></span>/<span id="pagecount"></span></div>
                    </div>
                    <div>
                        <h1 class="header">Oppfølgingsplanen</h1>
                    </div>

                    <div class="innhold">
                        <div class="innholdoverskrift innholdoverskrift--ingenborder">
                            <h2 class="left">Om denne planen</h2>
                        </div>
                        <label class="varseltekst"><b>Opprettet av: </b> <xsl:value-of select="opprettetAv"/></label><xsl:value-of select="opprettetDato"/>
                        <xsl:if test="visAdvarsel='true'">
                            <label class="varseltekst">Denne planen ble opprettet uten godkjenning fra den sykmeldte</label>
                        </xsl:if>
                        <xsl:if test="visAdvarsel='false'">
                            <label class="varseltekst"><b>Godkjent av:</b> <xsl:value-of select="godkjentAv"/></label> <label><xsl:value-of select="godkjentDato"/></label>
                        </xsl:if>
                        <label class="varseltekst"><b>Varer: </b> <xsl:value-of select="gyldigfra"/> - <xsl:value-of select="gyldigtil"/></label>
                        <label class="varseltekst"><b>Evalueres: </b> <xsl:value-of select="evalueres"/></label>
                    </div>

                    <div class="innhold">
                        <div class="innholdoverskrift">
                            <h2 class="left">Den sykmeldtes kontaktinformasjon</h2>
                        </div>
                        <table>
                            <tr>
                                <th>Navn</th>
                                <th>Fødselsnummer</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="$sykmeldtnavn"/></td>
                                <td><xsl:value-of select="sykmeldtFnr"/></td>
                            </tr>
                        </table>

                        <table>
                            <tr>
                                <th>Telefonnummer</th>
                                <th>E-post</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="sykmeldtTlf"/></td>
                                <td><xsl:value-of select="sykmeldtEpost"/></td>
                            </tr>
                        </table>
                        <table>
                            <tr>
                                <th>Stilling</th>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="stillingListe">
                                        <xsl:if test="prosent > -1">
                                            <xsl:value-of select="yrke"/>: <xsl:value-of select="prosent"/> %<br/>
                                        </xsl:if>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>

                    <div class="innhold">
                        <div class="innholdoverskrift">
                            <h2 class="left">Arbeidsgiverens kontaktinformasjon</h2>
                        </div>
                        <table>
                            <tr>
                                <th>Bedriftens navn</th>
                                <th>Navn på nærmeste leder</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="virksomhetsnavn"/></td>
                                <td><xsl:value-of select="$arbeidsgivernavn"/></td>
                            </tr>
                        </table>

                        <table>
                            <tr>
                                <th>Organisasjonsnummer</th>
                                <th>E-post</th>
                            </tr>
                            <tr>
                                <td><xsl:value-of select="arbeidsgiverOrgnr"/></td>
                                <td><xsl:value-of select="arbeidsgiverEpost"/></td>
                            </tr>
                        </table>

                        <table>
                            <tr>
                                <th></th>
                                <th>Telefonnummer</th>
                            </tr>
                            <tr>
                                <td></td>
                                <td><xsl:value-of select="arbeidsgiverTlf"/></td>
                            </tr>
                        </table>
                    </div>

                    <div class="innhold sykeforloepsperioder">
                        <div class="innholdoverskrift">
                            <h2 class="left">Informasjon fra dette sykefraværet</h2>
                        </div>
                        <table>
                            <tr>
                                <th>Sykmeldingsprosent</th>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="sykeforloepsperioderListe">
                                        <p><strong><xsl:value-of select="fom"/> &#8211; <xsl:value-of select="tom"/></strong> &#8226; <xsl:value-of select="antallDager"/> dager</p>
                                        <xsl:if test="gradering > 0">
                                            <xsl:choose>
                                                <xsl:when test="reisetilskudd and reisetilskudd='true'">
                                                    <p><xsl:value-of select="gradering"/> &#37; sykmeldt med reisetilskudd</p>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <p><xsl:value-of select="gradering"/> &#37; sykmeldt</p>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:if>
                                        <xsl:if test="behandlingsdager='true'">
                                            <p>Behandlingsdag(er)</p>
                                        </xsl:if>
                                        <xsl:if test="reisetilskudd='true' and gradering = 0">
                                            <p>Reisetilskudd</p>
                                        </xsl:if>
                                        <xsl:if test="avventende='true'">
                                            <P>Avventende sykmelding</P>
                                        </xsl:if>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>

                    <div class="innhold arbeidsoppgaver">
                        <xsl:if test="kanGjennomfoeresArbeidsoppgaveListe">
                            <div class="blokk--l">
                                <div class="bildeTittel">
                                    <img class="ikon" src="img/kan.jpg" />
                                    <h3 class="ikon-tittel">Arbeidsoppgaver som kan gjøres</h3>
                                </div>
                                <xsl:for-each select="kanGjennomfoeresArbeidsoppgaveListe">
                                    <div class="panel--ok">
                                        <h4><xsl:value-of select="navn"/></h4>
                                    </div>
                                </xsl:for-each>
                            </div>
                        </xsl:if>


                        <xsl:if test="kanGjennomfoeresMedTilretteleggingArbeidsoppgaveListe">
                            <div class="blokk--l">
                                <div class="bildeTittel">
                                    <img class="ikon" src="img/kanskje.jpg" />
                                    <h3 class="ikon-tittel">Arbeidsoppgaver som kan gjøres med tilrettelegging</h3>
                                </div>
                                <xsl:for-each select="kanGjennomfoeresMedTilretteleggingArbeidsoppgaveListe">
                                    <div class="panel--maybe">
                                        <h4><xsl:value-of select="navn"/></h4>
                                        <div class="arbeidsoppgaver_tilrettelegging">
                                            <xsl:if test="paaAnnetSted='true'">
                                                <span>Fra annet sted, </span>
                                            </xsl:if>
                                            <xsl:if test="medMerTid='true'">
                                                <span>Med mer tid, </span>
                                            </xsl:if>
                                            <xsl:if test="medHjelp='true'">
                                                <span>Med hjelp/hjelpemiddel, </span>
                                            </xsl:if>
                                        </div>
                                        <label class="arbeidsoppgave-sitat-label"><xsl:value-of select="$sykmeldtnavn"/>:</label>
                                        <q class="arbeidsoppgave-sitat">
                                            "<xsl:call-template name="replace">
                                            <xsl:with-param name="string" select="beskrivelse"/>
                                        </xsl:call-template>"
                                        </q>
                                    </div>
                                </xsl:for-each>
                            </div>
                        </xsl:if>


                        <xsl:if test="kanIkkeGjennomfoeresArbeidsoppgaveListe">
                            <div class="blokk--l">
                                <div class="bildeTittel">
                                    <img class="ikon" src="img/kan_ikke.jpg" />
                                    <h3>Arbeidsoppgaver som ikke kan gjøres</h3>
                                </div>
                                <xsl:for-each select="kanIkkeGjennomfoeresArbeidsoppgaveListe">
                                    <div class="panel--not">
                                        <h4><xsl:value-of select="navn"/></h4>
                                        <label class="arbeidsoppgave-sitat-label"><xsl:value-of select="$sykmeldtnavn"/></label>
                                        <q class="arbeidsoppgave-sitat">
                                            "<xsl:call-template name="replace">
                                            <xsl:with-param name="string" select="beskrivelse"/>
                                        </xsl:call-template>"
                                        </q>
                                    </div>
                                </xsl:for-each>
                            </div>
                        </xsl:if>

                        <xsl:if test="ikkeTattStillingTilArbeidsoppgaveListe">
                            <div class="blokk--l">
                                <div class="bildeTittel">
                                    <img class="ikon" src="img/varseltrekant.jpg" />
                                    <h3 class="ikon-tittel">Arbeidsoppgaver som ikke er blitt vurdert</h3>
                                </div>
                                <xsl:for-each select="ikkeTattStillingTilArbeidsoppgaveListe">
                                    <div class="panel--ingensvar">
                                        <h4><xsl:value-of select="navn"/></h4>
                                    </div>
                                </xsl:for-each>
                            </div>
                        </xsl:if>
                    </div>

                    <div class="innhold-tiltak">
                        <div class="innholdoverskrift innholdoverskrift--ingenborder">
                            <h3 class="left">Tiltak</h3>
                        </div>
                        <xsl:for-each select="tiltakListe">
                            <xsl:sort select="status" order="ascending"/>
                            <xsl:sort select="id" order="descending"/>
                            <div class="panel--tiltak">
                                <xsl:if test="not(status='IKKE_AKTUELT')">
                                    <p class="tiltak--dato"><xsl:value-of select="fom" /> - <xsl:value-of select="tom"/></p>
                                </xsl:if>
                                <h4><xsl:value-of select="navn"/></h4>
                                <xsl:if test="status='AVTALT'">
                                    <p class="status tiltakGodkjent">Avtalt</p>
                                </xsl:if>
                                <xsl:if test="status='FORSLAG'">
                                    <p class="status tiltakForslag">Foreslått</p>
                                </xsl:if>
                                <xsl:if test="status='IKKE_AKTUELT'">
                                    <p class="status tiltakIkkeAktuelt">Ikke aktuelt</p>
                                </xsl:if>

                                <xsl:if test="beskrivelse">
                                    <div class="tiltak-info">
                                        <label class="tiltak-sitat-label">BESKRIVELSE</label>
                                        <q class="tiltak-sitat tiltak-italic">
                                            "<xsl:call-template name="replace">
                                            <xsl:with-param name="string" select="beskrivelse"/>
                                        </xsl:call-template>"
                                        </q>
                                    </div>
                                </xsl:if>

                                <xsl:if test="opprettetAv">
                                    <div class="tiltak-info">
                                        <label class="tiltak-sitat-label">FORESLÅTT AV</label>
                                        <p class="tiltak-opprettetAv">
                                            <xsl:value-of select="opprettetAv"/>
                                        </p>
                                    </div>
                                </xsl:if>

                                <xsl:if test="gjennomfoering">
                                    <xsl:if test="status='AVTALT'">
                                        <div class="tiltak-info">
                                            <label class="tiltak-sitat-label">OPPFØLGING OG GJENNOMFØRING</label>
                                            <q class="tiltak-sitat tiltak-italic">
                                                "<xsl:call-template name="replace">
                                                <xsl:with-param name="string" select="gjennomfoering"/>
                                            </xsl:call-template>"
                                            </q>
                                        </div>
                                    </xsl:if>
                                </xsl:if>

                                <xsl:if test="beskrivelseIkkeAktuelt">
                                    <xsl:if test="status='IKKE_AKTUELT'">
                                        <div class="tiltak-info">
                                            <label class="tiltak-sitat-label">ARBEIDSGIVERS VURDERING</label>
                                            <q class="tiltak-sitat tiltak-italic">
                                                "<xsl:call-template name="replace">
                                                <xsl:with-param name="string" select="beskrivelseIkkeAktuelt"/>
                                            </xsl:call-template>"
                                            </q>
                                        </div>
                                    </xsl:if>
                                </xsl:if>
                            </div>
                        </xsl:for-each>
                    </div>
                </div>

            </body>
        </html>
    </xsl:template>

    <xsl:template name="replace">
        <xsl:param name="string"/>
        <xsl:choose>
            <xsl:when test="contains($string,'&#10;')">
                <xsl:value-of select="substring-before($string,'&#10;')"/>
                <br/>
                <xsl:call-template name="replace">
                    <xsl:with-param name="string" select="substring-after($string,'&#10;')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
