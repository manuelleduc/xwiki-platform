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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.index.internal.security.Review;
import org.xwiki.extension.index.internal.security.ReviewsMap;
import org.xwiki.extension.index.security.ExtensionSecurityAnalysisResult;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.security.ExtensionSecurityIndexationEndEvent;
import org.xwiki.extension.security.analyzer.ExtensionSecurityAnalyzer;
import org.xwiki.extension.security.internal.analyzer.VulnerabilityIndexer;
import org.xwiki.extension.security.internal.analyzer.osv.OsvExtensionSecurityAnalyzer;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.DefaultJobStatus;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Run a security analysis on the current instance's extensions.
 *
 * @version $Id$
 * @since 15.5RC1
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
@Named(ExtensionSecurityJob.JOBTYPE)
public class ExtensionSecurityJob
    extends AbstractJob<ExtensionSecurityRequest, DefaultJobStatus<ExtensionSecurityRequest>>
{
    /**
     * The id of the job.
     */
    public static final String JOBTYPE = "extension_security";

    @Inject
    private InstalledExtensionRepository installedExtensionRepository;

    @Inject
    private CoreExtensionRepository coreExtensionRepository;

    @Inject
    @Named(OsvExtensionSecurityAnalyzer.ID)
    private ExtensionSecurityAnalyzer extensionSecurityAnalyzer;

    @Inject
    private VulnerabilityIndexer vulnerabilityIndexer;

    @Override
    public String getType()
    {
        return JOBTYPE;
    }

    @Override
    protected void runInternal()
    {
        Collection<InstalledExtension> installedExtensions = this.installedExtensionRepository.getInstalledExtensions();
        Collection<CoreExtension> coreExtensions = this.coreExtensionRepository.getCoreExtensions();
        this.progressManager.pushLevelProgress(installedExtensions.size() + coreExtensions.size(), this);

        ReviewsMap reviewsMap = getReviewsMap();

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(10);

            List<Future<Boolean>> tasks = new ArrayList<>();
            for (InstalledExtension extension : installedExtensions) {
                tasks.add(executorService.submit(() -> handleExtension(extension, reviewsMap)));
            }

            for (CoreExtension extension : coreExtensions) {
                tasks.add(executorService.submit(() -> handleExtension(extension, reviewsMap)));
            }

            long newVulnerabilityCount = consumeTasks(tasks);
            this.observationManager.notify(new ExtensionSecurityIndexationEndEvent(), null, newVulnerabilityCount);
        } catch (InterruptedException e) {
            this.logger.warn("The job has been interrupted. Cause: [{}]", getRootCauseMessage(e));
            Thread.currentThread().interrupt();
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    private static ReviewsMap getReviewsMap()
    {
        // TODO: replace with a component, fetching this remotely.
        ReviewsMap reviewsMap = new ReviewsMap();
        Map<String, List<Review>> map = reviewsMap.getReviewsMap();
        String sourcePlatform = "xwiki-platform";
        String explanation1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus non pellentesque sem."
            + " Maecenas ultricies, nisi quis efficitur consectetur, libero enim blandit justo, at auctor nisi arcu "
            + "congue odio. ";
        String explanation2 = "ivamus eget condimentum elit, in blandit turpis. Nullam suscipit eros vitae justo "
            + "suscipit aliquam. Curabitur at nibh id elit eleifend condimentum eu a urna. Vivamus vel molestie nunc. "
            + "Suspendisse porta porta quam, vel pulvinar magna vulputate a. In porta tincidunt dui. Cras tortor ante, "
            + "interdum eget enim quis, posuere varius ligula.";
        String explanation3 = "Integer ligula eros, vulputate eu sem quis, rhoncus consequat tellus. Phasellus eget "
            + "tincidunt nibh. Ut luctus id dolor in dignissim. ";
        map.put("GHSA-2q8x-2p7f-574v", List.of(new Review(sourcePlatform, explanation1),
            new Review("some-extension", explanation2)));
        map.put("GHSA-3ccq-5vw3-2p6x", List.of(new Review(sourcePlatform, explanation2)));
        map.put("GHSA-64xx-cq4q-mf44", List.of(new Review(sourcePlatform, explanation3)));
        map.put("GHSA-6w62-hx7r-mw68", List.of(new Review(sourcePlatform, explanation1)));
        map.put("GHSA-6wf9-jmg9-vxcc", List.of(new Review(sourcePlatform, explanation2)));
        map.put("GHSA-8jrj-525p-826v", List.of(new Review(sourcePlatform, explanation3)));
        map.put("GHSA-cxfm-5m4g-x7xp", List.of(new Review(sourcePlatform, explanation1)));
        map.put("GHSA-f8cc-g7j8-xxpm", List.of(new Review(sourcePlatform, explanation2)));
        map.put("GHSA-g5w6-mrj7-75h2", List.of(new Review(sourcePlatform, explanation3)));
        map.put("GHSA-h7v4-7xg3-hxcc", List.of(new Review(sourcePlatform, explanation1)));
        map.put("GHSA-hph2-m3g5-xxv4", List.of(new Review(sourcePlatform, explanation2)));
        map.put("GHSA-j563-grx4-pjpv", List.of(new Review(sourcePlatform, explanation3)));
        map.put("GHSA-j9h8-phrw-h4fh", List.of(new Review(sourcePlatform, explanation1)));
        map.put("GHSA-p8pq-r894-fm8f", List.of(new Review(sourcePlatform, explanation2)));
        map.put("GHSA-qrx8-8545-4wg2", List.of(new Review(sourcePlatform, explanation3)));
        map.put("GHSA-rmr5-cpv2-vgjf", List.of(new Review(sourcePlatform, explanation1)));
        map.put("GHSA-xw4p-crpj-vjx2", List.of(new Review(sourcePlatform, explanation2)));
        map.put("GHSA-jv4x-j47q-6qvp", List.of(new Review(sourcePlatform, explanation3)));
        map.put("GHSA-2363-cqg2-863c", List.of(new Review(sourcePlatform, explanation1)));
        map.put("GHSA-58qw-p7qm-5rvh", List.of(new Review(sourcePlatform, explanation1)));
        return reviewsMap;
    }

    private long consumeTasks(List<Future<Boolean>> tasks) throws InterruptedException
    {
        long newVulnerabilityCount = 0;
        for (Future<Boolean> future : tasks) {
            try {
                Boolean updated = future.get();
                this.progressManager.startStep(this);
                if (Objects.equals(Boolean.TRUE, updated)) {
                    newVulnerabilityCount++;
                }
            } catch (ExecutionException e) {
                this.logger.error("Failed to execute an extension analysis.", e);
            } finally {
                this.progressManager.endStep(this);
            }
        }
        return newVulnerabilityCount;
    }

    private boolean handleExtension(Extension extension, ReviewsMap reviewsMap)
    {
        boolean hasNew = false;
        try {
            ExtensionSecurityAnalysisResult analysis = this.extensionSecurityAnalyzer.analyze(extension);
            if (analysis != null) {
                boolean update = this.vulnerabilityIndexer.update(extension, analysis, reviewsMap);
                if (update) {
                    hasNew = true;
                }
            }
        } catch (ExtensionSecurityException e) {
            this.logger.warn("Failed to analyse [{}]. Cause: [{}]", extension.getId(), getRootCauseMessage(e));
        } catch (Exception e) {
            this.logger.warn("Unexpected error [{}]", getRootCauseMessage(e));
        }
        return hasNew;
    }
}
