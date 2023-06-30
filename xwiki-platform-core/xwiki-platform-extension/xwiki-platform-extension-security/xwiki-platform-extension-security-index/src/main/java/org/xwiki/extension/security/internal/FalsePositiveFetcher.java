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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.index.internal.security.FalsePositiveMap;
import org.xwiki.extension.security.ExtensionSecurityConfiguration;
import org.xwiki.extension.security.internal.analyzer.osv.model.response.OsvResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * @version $Id$
 * @since 15.6RC1
 */
@Component(roles = FalsePositiveFetcher.class)
@Singleton
public class FalsePositiveFetcher
{
    @Inject
    private ExtensionSecurityConfiguration extensionSecurityConfiguration;

    @Inject
    private Logger logger;

    /**
     * @return {@link Optional#empty()} in case of {@link InterruptedException} during the HTTP request, the fetched
     *     {@link FalsePositiveMap} otherwise
     * @throws ExtensionSecurityException in case of issue when fetching the remove false-positive source
     */
    public Optional<FalsePositiveMap> fetch() throws ExtensionSecurityException
    {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(this.extensionSecurityConfiguration.getFalsePositiveURL()))
            .GET()
            .build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, ofString());
            return Optional.of(new ObjectMapper().readValue(response.body(), FalsePositiveMap.class));
        } catch (IOException e) {
            throw new ExtensionSecurityException("Failed to fetch the false positive.", e);
        } catch (InterruptedException e) {
            this.logger.warn("Can't finish the analysis as the thread was interrupted. Cause: [{}]",
                getRootCauseMessage(e));
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}
