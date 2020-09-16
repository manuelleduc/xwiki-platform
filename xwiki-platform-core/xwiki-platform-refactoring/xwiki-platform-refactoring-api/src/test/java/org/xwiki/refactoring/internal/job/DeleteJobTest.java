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
package org.xwiki.refactoring.internal.job;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.event.DocumentsDeletingEvent;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.job.Job;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.refactoring.RefactoringConfiguration;
import org.xwiki.refactoring.internal.batch.DefaultBatchOperationExecutor;
import org.xwiki.refactoring.job.EntityRequest;
import org.xwiki.refactoring.job.question.EntitySelection;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DeleteJob}.
 * 
 * @version $Id$
 */
@ComponentList(DefaultBatchOperationExecutor.class)
public class DeleteJobTest extends AbstractEntityJobTest
{
    @Rule
    public MockitoComponentMockingRule<Job> mocker = new MockitoComponentMockingRule<Job>(DeleteJob.class);

    private RefactoringConfiguration configuration;

    private DocumentAccessBridge documentAccessBridge;

    @Override
    protected MockitoComponentMockingRule<Job> getMocker()
    {
        return this.mocker;
    }

    @Override
    public void configure() throws Exception
    {
        super.configure();

        Execution execution = this.mocker.getInstance(Execution.class);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(execution.getContext()).thenReturn(executionContext);

        this.configuration = this.mocker.getInstance(RefactoringConfiguration.class);
        this.documentAccessBridge = this.mocker.getInstance(DocumentAccessBridge.class);
    }

    @Test
    public void deleteDocument() throws Throwable
    {
        DocumentReference documentReference = new DocumentReference("wiki", "Space", "Page");
        when(this.modelBridge.exists(documentReference)).thenReturn(true);

        DocumentReference userReference = new DocumentReference("wiki", "Users", "Alice");
        DocumentReference authorReference = new DocumentReference("wiki", "Users", "Bob");

        EntityRequest request = createRequest(documentReference);
        request.setCheckRights(false);
        request.setCheckAuthorRights(false);
        request.setUserReference(userReference);
        request.setAuthorReference(authorReference);
        run(request);

        verify(this.observationManager).notify(any(DocumentsDeletingEvent.class), any(DeleteJob.class),
            eq(Collections.singletonMap(documentReference, new EntitySelection(documentReference))));
        verify(this.modelBridge).setContextUserReference(userReference);
        verify(this.modelBridge).delete(documentReference);
    }

    @Test
    public void deleteDocumentSkipRecyclebin() throws Throwable
    {
        DocumentReference documentReference = new DocumentReference("wiki", "Space", "Page");
        when(this.modelBridge.exists(documentReference)).thenReturn(true);

        when(this.configuration.canSkipRecyclebin()).thenReturn(true);
        when(this.documentAccessBridge.isAdvancedUser()).thenReturn(true);

        DocumentReference userReference = new DocumentReference("wiki", "Users", "Alice");
        DocumentReference authorReference = new DocumentReference("wiki", "Users", "Bob");

        EntityRequest request = createRequest(documentReference);
        request.setCheckRights(false);
        request.setCheckAuthorRights(false);
        request.setUserReference(userReference);
        request.setAuthorReference(authorReference);
        request.setProperty("toRecyclebin", false);
        run(request);

        verify(this.observationManager).notify(any(DocumentsDeletingEvent.class), any(DeleteJob.class),
            eq(Collections.singletonMap(documentReference, new EntitySelection(documentReference))));
        verify(this.modelBridge).setContextUserReference(userReference);
        verify(this.modelBridge).expurge(documentReference);
    }

    @Test
    public void deleteMissingDocument() throws Throwable
    {
        DocumentReference documentReference = new DocumentReference("wiki", "Space", "Page");
        run(createRequest(documentReference));
        verify(this.mocker.getMockedLogger()).warn("Skipping [{}] because it doesn't exist.", documentReference);
        verify(this.modelBridge, never()).delete(any(DocumentReference.class));
    }

    @Test
    public void deleteDocumentWithoutDeleteRightUser() throws Throwable
    {
        DocumentReference documentReference = new DocumentReference("wiki", "Space", "Page");
        when(this.modelBridge.exists(documentReference)).thenReturn(true);

        DocumentReference userReference = new DocumentReference("wiki", "Users", "Alice");
        when(this.authorization.hasAccess(Right.DELETE, userReference, documentReference)).thenReturn(false);

        DocumentReference authorReference = new DocumentReference("wiki", "Users", "Bob");
        when(this.authorization.hasAccess(Right.DELETE, authorReference, documentReference)).thenReturn(true);

        EntityRequest request = createRequest(documentReference);
        request.setCheckRights(true);
        request.setCheckAuthorRights(true);
        request.setUserReference(userReference);
        request.setAuthorReference(authorReference);
        run(request);

        verify(this.mocker.getMockedLogger()).error("You are not allowed to delete [{}].", documentReference);
        verify(this.modelBridge, never()).delete(any(DocumentReference.class));
    }

    @Test
    public void deleteDocumentWithoutDeleteRightAuthor() throws Throwable
    {
        DocumentReference documentReference = new DocumentReference("wiki", "Space", "Page");
        when(this.modelBridge.exists(documentReference)).thenReturn(true);

        DocumentReference userReference = new DocumentReference("wiki", "Users", "Alice");
        when(this.authorization.hasAccess(Right.DELETE, userReference, documentReference)).thenReturn(true);

        DocumentReference authorReference = new DocumentReference("wiki", "Users", "Bob");
        when(this.authorization.hasAccess(Right.DELETE, authorReference, documentReference)).thenReturn(false);

        EntityRequest request = createRequest(documentReference);
        request.setCheckRights(true);
        request.setCheckAuthorRights(true);
        request.setUserReference(userReference);
        request.setAuthorReference(authorReference);
        run(request);

        verify(this.mocker.getMockedLogger()).error("You are not allowed to delete [{}].", documentReference);
        verify(this.modelBridge, never()).delete(any(DocumentReference.class));
    }

    @Test
    public void deleteSpaceHomeDeep() throws Throwable
    {
        DocumentReference documentReference = new DocumentReference("wiki", "Space", "WebHome");
        EntityRequest request = createRequest(documentReference);
        request.setDeep(true);
        run(request);

        // We only verify if the job fetches the documents from the space. The rest of the test is in #deleteSpace()
        verify(this.modelBridge, atLeastOnce()).getDocumentReferences(documentReference.getLastSpaceReference());
    }

    @Test
    public void deleteSpace() throws Throwable
    {
        SpaceReference spaceReference = new SpaceReference("Space", new WikiReference("wiki"));
        DocumentReference aliceReference = new DocumentReference("wiki", "Space", "Alice");
        DocumentReference bobReference = new DocumentReference("wiki", "Space", "Bob");
        when(this.modelBridge.getDocumentReferences(spaceReference)).thenReturn(
            Arrays.asList(aliceReference, bobReference));

        run(createRequest(spaceReference));

        // We only verify that the code tries to delete the documents.
        verify(this.mocker.getMockedLogger()).warn("Skipping [{}] because it doesn't exist.", aliceReference);
        verify(this.mocker.getMockedLogger()).warn("Skipping [{}] because it doesn't exist.", bobReference);
    }

    @Test
    public void deleteUnsupportedEntity() throws Throwable
    {
        run(createRequest(new WikiReference("foo")));
        verify(this.mocker.getMockedLogger(), times(2)).error("Unsupported entity type [{}].", EntityType.WIKI);
        verify(this.modelBridge, never()).delete(any(DocumentReference.class));
    }

    private EntityRequest createRequest(EntityReference... entityReference)
    {
        EntityRequest request = new EntityRequest();
        request.setEntityReferences(Arrays.asList(entityReference));
        return request;
    }
}
