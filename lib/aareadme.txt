Directories
===========
lib - utility jars
lib/api - Directory where API spec libraries live.
lib/doc - jars needed for generating documentation. Not included with JMeter releases.
lib/ext - JMeter jars only
lib/junit - test jar for JUnit sampler
lib/opt - Directory where Optional 3rd party libraries live
lib/src - storage area for source and javadoc jars, e.g. for use in IDEs
          Excluded from SVN, not included in classpath

Which jars are used by which modules?
====================================
[not exhaustive]

asm-5.1 (org.ow2.asm)
----------------------
- JSON Path extractor

accessors-smart-1.1 (net.minidev)
----------------------
- JSON Path extractor

avalon-framework-4.1.4 (org.apache.avalon.framework)
----------------------
- LogKit (LoggingManager)
- Configuration (DataSourceElement)
- OldSaveService

bsf-2.4.0.jar (org.apache.bsf)
-------------
http://jakarta.apache.org/site/downloads/downloads_bsf.cgi
- BSF test elements (sampler etc.)

bsh-2.0b5.jar (org.bsh)
-------------
- BeanShell test elements

commons-codec-1.10
-----------------
http://commons.apache.org/downloads/download_codec.cgi
- used by commons-httpclient-3.1
- also HtmlParserTester for Base64

commons-collections-3.2.2
-------------------------
http://commons.apache.org/downloads/download_collections.cgi
- ListenerNotifier
- Anakia

commons-httpclient-3.1
----------------------
http://hc.apache.org/downloads.cgi
- httpclient version of HTTP sampler
- Cookie manager implementation

commons-io-2.5
--------------
http://commons.apache.org/downloads/download_io.cgi
- FTPSampler

commons-jexl-2.1.1, commons-jexl3-3.0
----------------
http://commons.apache.org/downloads/download_jexl.cgi
- Jexl function and BSF test elements

commons-lang-2.6
----------------
http://commons.apache.org/downloads/download_lang.cgi
- velocity (Anakia)

commons-lang3-3.5
----------------
http://commons.apache.org/downloads/download_lang.cgi
- URLCollection (unescapeXml)

commons-logging-1.2
---------------------
http://commons.apache.org/downloads/download_logging.cgi
- httpclient

commons-math3-3.6.1
-----------------
http://commons.apache.org/proper/commons-math/download_math.cgi
- BackendListener

commons-net-3.5
-----------------
http://commons.apache.org/downloads/download_net.cgi
- FTPSampler

commons-pool2-2.4.2
-----------------
http://commons.apache.org/proper/commons-pool/download_pool.cgi
- BackendListener

dnsjava-2.1.7
-----------------
http://www.dnsjava.org/download/
- DNSCacheManager

excalibur-logger-1.1 (org.apache.avalon.excalibur.logger)
--------------------
- LoggingManager

groovy-all-2.4.7
----------------------
Advised scripting language for JSR223 Test Elements

hamcrest-core-1.3
----------------------
- unit tests, JUnit sampler
https://github.com/hamcrest/JavaHamcrest

freemarker-2.3.23.jar
----------------------
- used by Report/Dashboard feature

jCharts-0.7.5 (org.jCharts)
-------------
http://jcharts.sourceforge.net/downloads.html
- AxisGraph,LineGraph,LineChart

jdom-1.1.3
--------
http://www.jdom.org/downloads/index.html
- Anakia

jodd-core-3.7.1
--------
http://www.jodd.org/
- CSS/JQuery like extractor dependency

jodd-lagarto-3.7.1
--------
http://jodd.org/doc/csselly/
- CSS/JQuery like extractor

jodd-log-3.7.1
--------
http://www.jodd.org/
- CSS/JQuery like extractor dependency

jodd-props-3.7.1
--------
http://www.jodd.org/
- used by Report/Dashboard feature properties management

json-path-2.2.0
--------
https://github.com/jayway/JsonPath
- JSON Path Extractor
- JSON Path Renderer

json-smart-2.2.1 (net.minidev)
--------
https://github.com/netplex/json-smart-v2
- JSON Path Extractor
- JSON Path Renderer

jsoup-1.10.1
--------
http://www.jsoup.org/
- CSS/JQuery like extractor

ph-css-4.1.6
--------
https://github.com/phax/ph-css
- CssParser

ph-commons-6.2.4
--------
https://github.com/phax/ph-commons
- CssParser

rhino-1.7.7.1
--------
http://www.mozilla.org/rhino/download.html
- javascript function
- IfController
- WhileController
- BSF (Javascript)

jTidy-r938
----
- http: various modules for parsing html
- org.xml.sax - various
- XPathUtil (XPath assertion)

junit 4.12
-----------
- unit tests, JUnit sampler

HttpComponents (HttpComponents Core 4.4.x and HttpComponents Client 4.5.x)
-----------
http://hc.apache.org/
- httpclient 4 implementation for HTTP sampler 

logkit-2.0
----------
- logging
- Anakia

mongo-java-driver 2.11.3
------------------------
http://www.mongodb.org/
- MongoDB sampler

oro-2.0.8
---------
http://jakarta.apache.org/site/downloads/downloads_oro.cgi
- regular expressions: various

rsyntaxtextarea-2.6.0
---------------------
http://fifesoft.com/rsyntaxtextarea/
- syntax coloration

serialiser-2.7.1
----------------
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
- xalan

slf4j-api-1.7.21
----------------
http://www.slf4j.org/
- jodd-core
- json-path

tika-1.14
--------------
http://tika.apache.org/
- Regular Expression Extractor

commons-dbcp2-2.1.1 (org.apache.commons.dbcp2)
--------------------------
- DataSourceElement (JDBC)

velocity-1.7
--------------
http://velocity.apache.org/download.cgi
- Anakia (create documentation) Not used by JMeter runtime

xalan_2.7.1
-----------
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
+org.apache.xalan|xml|xpath

xercesimpl-2.11.0
----------------
http://xerces.apache.org/xerces2-j/download.cgi
+org.apache.html.dom|org.apache.wml|org.apache.xerces|org.apache.xml.serialize
+org.w3c.dom.html|ls

xml-apis-1.4.01
--------------
http://xerces.apache.org/xerces2-j/download.cgi
+javax.xml
+org.w3c.dom
+org.xml.sax

The x* jars above are used for XML handling

xmlgraphics-commons-2.1 (org.apache.xmlgraphics.image.codec)
------------------
http://xmlgraphics.apache.org/commons/download.html
- SaveGraphicsService

xmlpull-1.1.3.1
---------------
http://www.xmlpull.org/impls.shtml
- xstream


xpp3_min-1.1.4c
---------------
http://x-stream.github.io/download.html
or
http://www.extreme.indiana.edu/dist/java-repository/xpp3/distributions/
- xstream

xstream-1.4.9
-------------
http://x-stream.github.io/download.html
- SaveService
