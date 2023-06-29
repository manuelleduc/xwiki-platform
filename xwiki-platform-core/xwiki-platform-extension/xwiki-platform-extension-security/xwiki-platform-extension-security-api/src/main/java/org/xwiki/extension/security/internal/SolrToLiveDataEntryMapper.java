/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.extension.security.internal;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.solr.common.SolrDocument;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.index.internal.ExtensionIndexStore;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.search.solr.SolrUtils;
import org.xwiki.search.solr.internal.api.FieldUtils;
import org.xwiki.template.TemplateManager;

import static com.xpn.xwiki.web.ViewAction.VIEW_ACTION;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static java.util.stream.Collectors.joining;
import static javax.script.ScriptContext.ENGINE_SCOPE;
import static org.apache.commons.lang.StringEscapeUtils.escapeXml;
import static org.xwiki.extension.index.internal.ExtensionIndexSolrCoreInitializer.IS_IGNORED;
import static org.xwiki.extension.index.internal.ExtensionIndexSolrCoreInitializer.SECURITY_ADVICE;
import static org.xwiki.extension.index.internal.ExtensionIndexSolrCoreInitializer.SECURITY_CVE_CVSS;
import static org.xwiki.extension.index.internal.ExtensionIndexSolrCoreInitializer.SECURITY_CVE_ID;
import static org.xwiki.extension.index.internal.ExtensionIndexSolrCoreInitializer.SECURITY_CVE_LINK;
import static org.xwiki.extension.index.internal.ExtensionIndexSolrCoreInitializer.SECURITY_FIX_VERSION;
import static org.xwiki.extension.index.internal.ExtensionIndexSolrCoreInitializer.SECURITY_MAX_CVSS;
import static org.xwiki.extension.index.internal.ExtensionIndexSolrCoreInitializer.SOLR_FIELD_ID;
import static org.xwiki.extension.security.internal.livedata.ExtensionSecurityLiveDataConfigurationProvider.ADVICE;
import static org.xwiki.extension.security.internal.livedata.ExtensionSecurityLiveDataConfigurationProvider.CVE_ID;
import static org.xwiki.extension.security.internal.livedata.ExtensionSecurityLiveDataConfigurationProvider.FIX_VERSION;
import static org.xwiki.extension.security.internal.livedata.ExtensionSecurityLiveDataConfigurationProvider.MAX_CVSS;
import static org.xwiki.extension.security.internal.livedata.ExtensionSecurityLiveDataConfigurationProvider.NAME;
import static org.xwiki.extension.security.internal.livedata.ExtensionSecurityLiveDataConfigurationProvider.WIKIS;

/**
 * Converts a {@link SolrDocument} to a {@link Map} of Live Data entries.
 *
 * @version $Id$
 * @since 15.5RC1
 */
@Component(roles = SolrToLiveDataEntryMapper.class)
@Singleton
public class SolrToLiveDataEntryMapper
{
    @Inject
    private SolrUtils solrUtils;

    @Inject
    private DocumentAccessBridge documentAccessBridge;

    @Inject
    private ContextualLocalizationManager l10n;

    @Inject
    private ExtensionIndexStore extensionIndexStore;

    @Inject
    private ScriptContextManager scriptContextManager;

    @Inject
    private TemplateManager templateManager;

    /**
     * @param doc the document to convert to Live Data entries.
     * @return Converts a {@link SolrDocument} to a {@link Map} of Live Data entries.
     */
    public Map<String, Object> mapDocToEntries(SolrDocument doc)
    {
        return ofEntries(
            entry(NAME, buildExtensionName(doc)),
            entry(MAX_CVSS, buildMaxCVSS(doc)),
            entry(CVE_ID, buildCVEList(doc)),
            entry(FIX_VERSION, buildFixVersion(doc)),
            entry(ADVICE, buildAdvice(doc)),
            entry(WIKIS, buildWikis(doc))
        );
    }

    private String buildCVEList(SolrDocument doc)
    {
        List<String> cveIds = mapToStrings(doc, SECURITY_CVE_ID);
        this.scriptContextManager.getCurrentScriptContext().setAttribute("cveIds", cveIds, ENGINE_SCOPE);
        List<String> cveLinks = mapToStrings(doc, SECURITY_CVE_LINK);
        this.scriptContextManager.getCurrentScriptContext().setAttribute("cveLinks", cveLinks, ENGINE_SCOPE);
        List<String> cveCVSS = mapToStrings(doc, SECURITY_CVE_CVSS);
        this.scriptContextManager.getCurrentScriptContext().setAttribute("cveCVSS", cveCVSS, ENGINE_SCOPE);
        
        List<Boolean> ignored = Optional.ofNullable(doc.getFieldValues(IS_IGNORED))
            .map(values -> values.stream()
                .map(it -> (boolean) it)
                .collect(Collectors.toList()))
            .orElse(List.of());

        List<Integer> notIgnoredCVEsIndex = IntStream.range(0, cveIds.size())
            .filter(((IntPredicate) ignored::get).negate())
            .boxed()
            .collect(Collectors.toList());
        this.scriptContextManager.getCurrentScriptContext()
            .setAttribute("notIgnoredCVEsIndex", notIgnoredCVEsIndex, ENGINE_SCOPE);

        List<Integer> ignoredCVEsIndex = IntStream.range(0, cveIds.size())
            .filter(ignored::get)
            .boxed()
            .collect(Collectors.toList());
        this.scriptContextManager.getCurrentScriptContext()
            .setAttribute("ignoredCVEsIndex", ignoredCVEsIndex, ENGINE_SCOPE);
//
//        String ignoredStr = IntStream.range(0, cveIds.size())
//            .filter(ignored::get)
//            .mapToObj(ignoreCveTemplate(cveIds, cveLinks, cveCVSS))
//            .collect(joining(newLineHtml));
//
//        if (StringUtils.isNotEmpty(ignoredStr) && StringUtils.isNotEmpty(notIgnored)) {
//            return notIgnored + newLineHtml + "<span class='xHint'>Ignored:" + newLineHtml + ignoredStr + "</span>";
//        } else if (StringUtils.isNotEmpty(notIgnored)) {
//            return notIgnored;
//        } else {
//            return ignoredStr;
//        }

        String render;
        try {
            render = this.templateManager.render("extension/security/cveID.vm");
        } catch (Exception e) {
            // TODO...
            throw new RuntimeException(e);
        }
        return render;
    }

    private static IntFunction<String> cveTemplate(List<String> cveIds, List<String> cveLinks,
        List<String> cveCVSS)
    {
        return value -> String.format("<a href='%s'>%s</a>&nbsp;(%s)",
            escapeXml(cveLinks.get(value)),
            escapeXml(cveIds.get(value)),
            escapeXml(cveCVSS.get(value)));
    }

    private static IntFunction<String> ignoreCveTemplate(List<String> cveIds, List<String> cveLinks,
        List<String> cveCVSS)
    {
        return value -> {
            String link = cveLinks.get(value);
            String cveId = cveIds.get(value);
            return String.format("<a href='%s'>%s</a>&nbsp;(%s) "
                    + "<button type=\"button\" class=\"btn btn-default btn-xs\" data-toggle=\"modal\" "
                    + "data-target=\"[data-vulnerability-modal='%s']\">"
                    + "<span class=\"fa fa-file-text-o\"></span>"
                    + "</button><div class=\"modal fade bs-example-modal-lg\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"myLargeModalLabel\" data-vulnerability-modal='%s'>\n"
                    + "    <div class=\"modal-dialog modal-lg\" role=\"document\">\n"
                    + "      <div class=\"modal-content\">\n"
                    + "\n"
                    + "        <div class=\"modal-header\">\n"
                    + "          <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">×</span></button>\n"
                    + "          <h4 class=\"modal-title\">Vulnerability GHSA-2q8x-2p7f-574v of com.thoughtworks.xstream:xstream/1.4.17</h4>\n"
                    + "        </div>\n"
                    + "        <div class=\"modal-body\">\n"
                    + "          <dl>\n"
                    + "            <dt>xwiki-platform</dt>\n"
                    + "            <dd> Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum varius tortor vitae velit semper, non viverra sapien fringilla. Donec luctus tellus a neque tempus pharetra. Morbi at interdum tortor. Quisque ullamcorper vitae nibh porta venenatis. Pellentesque facilisis commodo rutrum. Praesent eu enim eleifend, maximus elit sed, tincidunt augue. Etiam id sapien sapien. Nulla facilisi. In hac habitasse platea dictumst. Sed pretium finibus libero porta faucibus. Aliquam enim risus, lacinia vel quam ac, posuere maximus sapien. Mauris ut consectetur erat. </dd>\n"
                    + "          </dl>\n"
                    + "          <dl>\n"
                    + "            <dt>application-xxx-yyy</dt>\n"
                    + "            <dd>Nam vulputate magna at turpis tristique, sit amet varius sem aliquet. Nunc sodales vulputate faucibus. Aliquam eget lorem est. Quisque vel leo eget arcu interdum volutpat a sagittis nulla.</dd>\n"
                    + "          </dl>\n"
                    + "        </div>\n"
                    + "      </div><!-- /.modal-content -->\n"
                    + "    </div><!-- /.modal-dialog -->\n"
                    + "  </div>",
                escapeXml(link),
                escapeXml(cveId),
                escapeXml(cveCVSS.get(value)),
                escapeXml(cveId),
                escapeXml(cveId)
            );
        };
    }

    private static List<String> mapToStrings(SolrDocument doc, String name)
    {
        Collection<Object> fieldValues = doc.getFieldValues(name);
        if (fieldValues == null) {
            return List.of();
        }
        return fieldValues.stream().map(String::valueOf).collect(Collectors.toList());
    }

    private String buildAdvice(SolrDocument doc)
    {
        String translationPlain = this.l10n.getTranslationPlain(this.solrUtils.get(SECURITY_ADVICE, doc));
        if (translationPlain == null) {
            return "";
        }
        return translationPlain;
    }

    private String buildFixVersion(SolrDocument doc)
    {
        Object o = doc.get(SECURITY_FIX_VERSION);
        if (o == null) {
            return "";
        }
        return String.valueOf(o);
    }

    private Double buildMaxCVSS(SolrDocument doc)
    {
        return this.solrUtils.get(SECURITY_MAX_CVSS, doc);
    }

    private String buildExtensionId(SolrDocument doc)
    {
        return this.solrUtils.get(SOLR_FIELD_ID, doc);
    }

    private String buildExtensionName(SolrDocument doc)
    {
        String extensionName;
        // If the extension does not have a name and version indexed, we fall back to an extensionId parsing.
        // Note: this is not supposed to happen in practice.
        ExtensionId extensionId = this.extensionIndexStore.getExtensionId(doc);
        List<BasicNameValuePair> parameters =
            buildExtensionURLParameters(extensionId.getId(), extensionId.getVersion().getValue());
        if (doc.get(FieldUtils.NAME) != null) {
            extensionName = this.solrUtils.get(FieldUtils.NAME, doc);
        } else {
            // Fallback to the id in case the name is empty.
            String[] versionId = this.solrUtils.getId(doc).split("/");
            extensionName = versionId[0];
        }
        String url = this.documentAccessBridge.getDocumentURL(new LocalDocumentReference("XWiki", "Extensions"),
            VIEW_ACTION, URLEncodedUtils.format(parameters, Charset.defaultCharset()), null);

        String extensionNameEscaped = escapeXml(String.valueOf(extensionName));
        String extensionIdEscaped = escapeXml(buildExtensionId(doc));
        return String.format("<a href='%s' title='%s'>%s</a><br/><span class='xHint' title='%s'>%s</span>",
            escapeXml(url),
            extensionNameEscaped,
            extensionNameEscaped,
            extensionIdEscaped,
            extensionIdEscaped
        );
    }

    private static List<BasicNameValuePair> buildExtensionURLParameters(String extensionId, String extensionVersion)
    {
        return List.of(
            new BasicNameValuePair("section", "XWiki.Extensions"),
            new BasicNameValuePair("extensionId", extensionId),
            new BasicNameValuePair("extensionVersion", extensionVersion)
        );
    }

    private String buildWikis(SolrDocument doc)
    {
        List<Object> list = this.solrUtils.getList(InstalledExtension.FIELD_INSTALLED_NAMESPACES, doc);
        if (list == null) {
            return "";
        }
        return list.stream()
            .map(String::valueOf)
            .map(it -> it.replaceFirst("wiki:", ""))
            .collect(joining(", "));
    }
}
