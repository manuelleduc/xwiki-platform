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


/**
 * Vue event bus for the edit action states.
 * Sent events:
 * - start-editing-entry({entryId, propertyId}): when an entry passes in edit mode
 * - cancel-editing-entry({entryId, propertyId}): when an entry goes out of edit mode without saving its edits
 * - save-editing-entry({entryId, propertyId, content}): when an entry goes out of edit model and wants to save its 
 *   edits. The content is an arbitrary value specific to the property's displayer type. 
 */
define('edit-bus', ['Vue'], (Vue) => {
  return new Vue();
})