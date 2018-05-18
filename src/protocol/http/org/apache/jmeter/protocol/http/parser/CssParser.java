/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.protocol.http.parser;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpressionMemberTermURI;
import com.helger.css.decl.CSSImportRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.ICSSTopLevelRule;
import com.helger.css.decl.visit.CSSVisitor;
import com.helger.css.decl.visit.DefaultCSSUrlVisitor;
import com.helger.css.handler.LoggingCSSParseExceptionCallback;
import com.helger.css.parser.ParseException;
import com.helger.css.reader.CSSReader;
import com.helger.css.reader.CSSReaderSettings;
import com.helger.css.reader.errorhandler.DoNothingCSSInterpretErrorHandler;
import com.helger.css.reader.errorhandler.ICSSInterpretErrorHandler;
import com.helger.css.reader.errorhandler.LoggingCSSParseErrorHandler;

/**
 * CSS Parser used to extract from CSS files external urls
 * @since 3.0
 */
public class CssParser implements LinkExtractorParser {
    private static final boolean IGNORE_UNRECOVERABLE_PARSING_ERROR = JMeterUtils.getPropDefault("httpsampler.ignore_failed_embedded_resource", false); //$NON-NLS-1$
    private static final Logger LOG = LoggingManager.getLoggerForClass();

    /**
     *
     */
    private static final int CSS_URL_CACHE_MAX_SIZE = JMeterUtils.getPropDefault("css.parser.cache.size", 400);
    private static final boolean IGNORE_ALL_CSS_ERRORS = JMeterUtils.getPropDefault("css.parser.ignore_all_css_errors", true);

    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static final Map<String, URLCollection> CSS_URL_CACHE =
            CSS_URL_CACHE_MAX_SIZE > 0 ? Collections.synchronizedMap(new LRUMap(CSS_URL_CACHE_MAX_SIZE)) : null;


    private static final class CustomLoggingCSSParseExceptionCallback extends LoggingCSSParseExceptionCallback {
        /**
         *
         */
        private static final long serialVersionUID = -9111232037888068394L;
        private URL cssUrl;

        /**
         * @param cssUrl {@link URL}
         */
        public CustomLoggingCSSParseExceptionCallback(URL cssUrl) {
            this.cssUrl = cssUrl;
        }
        /**
         * @see com.helger.css.handler.LoggingCSSParseExceptionCallback#onException(com.helger.css.parser.ParseException)
         */
        @Override
        public void onException(ParseException ex) {
            if(IGNORE_UNRECOVERABLE_PARSING_ERROR) {
                LOG.warn("Failed to parse CSS: " + cssUrl + ", " + LoggingCSSParseErrorHandler.createLoggingStringParseError (ex));
            } else {
                throw new IllegalStateException("Failed to parse CSS: " + cssUrl + ", " + LoggingCSSParseErrorHandler.createLoggingStringParseError (ex));
            }
        }
    }

    /**
     *
     */
    public CssParser() {
    }

    /**
     *
     * @see
     * org.apache.jmeter.protocol.http.parser.LinkExtractorParser#getEmbeddedResourceURLs
     * (java.lang.String, byte[], java.net.URL, java.lang.String)
     */
    @Override
    public Iterator<URL> getEmbeddedResourceURLs(String userAgent, byte[] data,
            final URL baseUrl, String encoding) throws LinkExtractorParseException {
        try {
            boolean cacheEnabled = CSS_URL_CACHE_MAX_SIZE > 0;
            String md5Key = null;
            URLCollection urlCollection = null;
            if(cacheEnabled) {
                md5Key = DigestUtils.md5Hex(data);
                urlCollection = CSS_URL_CACHE.get(md5Key);
            }

            if(urlCollection == null) {
                String cssContent = new String(data, encoding);
                final CSSReaderSettings cssSettings = new CSSReaderSettings()
                        .setBrowserCompliantMode(true)
                        .setFallbackCharset(Charset.forName(encoding))
                        .setCSSVersion(ECSSVersion.CSS30)
                        .setCustomErrorHandler(
                                new LoggingCSSParseErrorHandler())
                        .setCustomExceptionHandler(
                                new CustomLoggingCSSParseExceptionCallback(
                                        baseUrl));
                if (IGNORE_ALL_CSS_ERRORS) {
                    cssSettings
                            .setInterpretErrorHandler(new DoNothingCSSInterpretErrorHandler());
                }
                final CascadingStyleSheet aCSS = CSSReader
                        .readFromStringStream(cssContent, cssSettings);
                final List<URLString> list = new ArrayList<>();
                urlCollection = new URLCollection(list);
                final URLCollection localCollection = urlCollection;
                if(aCSS != null) {
                    CSSVisitor.visitCSSUrl(aCSS, new DefaultCSSUrlVisitor() {
                        @Override
                        public void onImport(final CSSImportRule importRule) {
                            String location = importRule.getLocationString();
                            if(!StringUtils.isEmpty(location)) {
                                localCollection.addURL(location, baseUrl);
                            }
                        }
                        // Call for URLs outside of URLs
                        @Override
                        public void onUrlDeclaration(
                                final ICSSTopLevelRule aTopLevelRule,
                                final CSSDeclaration aDeclaration,
                                final CSSExpressionMemberTermURI aURITerm) {
                            // NOOP
                            // Browser fetch such urls only when CSS rule matches
                            // so we disable this code
                            //urlCollection.addURL(aURITerm.getURIString(), baseUrl);
                        }
                    });
                    if(cacheEnabled) {
                        CSS_URL_CACHE.put(md5Key, urlCollection);
                    }
                } else {
                   LOG.warn("Failed parsing url:"+baseUrl+", got null CascadingStyleSheet");
                }
            }

            if(LOG.isDebugEnabled()) {
                StringBuilder builder = new StringBuilder();
                for (Iterator<URL> iterator = urlCollection.iterator(); iterator.hasNext();) {
                    URL urlString = iterator.next();
                    builder.append(urlString).append(',');
                }
                LOG.debug("Parsed:"+baseUrl+", got:"+builder.toString());
            }

            return urlCollection.iterator();
        } catch (Exception e) {
            throw new LinkExtractorParseException(e);
        }
    }

    @Override
    public boolean isReusable() {
        return true;
    }

}
