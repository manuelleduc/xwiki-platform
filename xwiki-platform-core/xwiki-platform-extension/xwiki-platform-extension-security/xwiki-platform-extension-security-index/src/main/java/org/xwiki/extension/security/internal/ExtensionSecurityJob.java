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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
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

        // TODO: replace with a component, fetching this remotely.
        Set<String> falsePositiveCVEs = Set.of(
            "GHSA-2q8x-2p7f-574v",
            "GHSA-3ccq-5vw3-2p6x",
            "GHSA-64xx-cq4q-mf44",
            "GHSA-6w62-hx7r-mw68",
            "GHSA-6wf9-jmg9-vxcc",
            "GHSA-8jrj-525p-826v",
            "GHSA-cxfm-5m4g-x7xp",
            "GHSA-f8cc-g7j8-xxpm",
            "GHSA-g5w6-mrj7-75h2",
            "GHSA-h7v4-7xg3-hxcc",
            "GHSA-hph2-m3g5-xxv4",
            "GHSA-j563-grx4-pjpv",
            "GHSA-j9h8-phrw-h4fh",
            "GHSA-p8pq-r894-fm8f",
            "GHSA-qrx8-8545-4wg2",
            "GHSA-rmr5-cpv2-vgjf",
            "GHSA-xw4p-crpj-vjx2",
            "GHSA-gx2c-fvhc-ph4j",
            "GHSA-rmpj-7c96-mrg8"
        );

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            // Note: for now, this step is sequential and each extension is analyzed after the previous one.
            long newVulnerabilityCount = 0;
            for (InstalledExtension extension : installedExtensions) {
                tasks.add(() -> handleExtension(extension, falsePositiveCVEs));
            }

            for (CoreExtension extension : coreExtensions) {
                tasks.add(() -> handleExtension(extension, falsePositiveCVEs));
            }

            List<Future<Boolean>> futures = executorService.invokeAll(tasks);
            for (Future<Boolean> future : futures) {
                try {
                    if (Objects.equals(Boolean.TRUE, future.get())) {
                        newVulnerabilityCount++;
                    }
                } catch (ExecutionException e) {
                    // TODO...
                    throw new RuntimeException(e);
                }
            }

            this.observationManager.notify(new ExtensionSecurityIndexationEndEvent(), null, newVulnerabilityCount);
        } catch (InterruptedException e) {
            // TODO...
            throw new RuntimeException(e);
        } finally {
            this.progressManager.popLevelProgress(this);
        }
    }

    private boolean handleExtension(Extension extension, Set<String> falsePositiveCVEs)
    {
        boolean hasNew = false;
        this.progressManager.startStep(this);
        try {
            ExtensionSecurityAnalysisResult analysis = this.extensionSecurityAnalyzer.analyze(extension);
            if (analysis != null) {
                boolean update = this.vulnerabilityIndexer.update(extension, analysis, falsePositiveCVEs);
                if (update) {
                    hasNew = true;
                }
            }
        } catch (ExtensionSecurityException e) {
            this.logger.warn("Failed to analyse [{}]. Cause: [{}]", extension.getId(), getRootCauseMessage(e));
        } catch (Exception e) {
            this.logger.warn("Unexpected error [{}]", getRootCauseMessage(e));
        }
        this.progressManager.endStep(this);
        return hasNew;
    }
}
