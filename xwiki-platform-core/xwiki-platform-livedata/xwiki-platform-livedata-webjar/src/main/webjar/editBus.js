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

  var _logic = undefined;
  var _editBus = new Vue();

  class EditBusService {

    constructor(editBus, logic) {
      this.editBus = editBus;
      this.editStates = {};
      this.editStatesTimeouts = {};
      this.logic = logic;
    }

    init() {
      this.editBus.$on('start-editing-entry', ({entryId, propertyId}) => {
        const entryState = this.editStates[entryId] || {};
        const propertyState = entryState[propertyId] || {};
        propertyState.editing = true;
        entryState[propertyId] = propertyState;
        this.editStates[entryId] = entryState;
      })

      this.editBus.$on('cancel-editing-entry', ({entryId, propertyId}) => {
        const entryState = this.editStates[entryId];
        const propertyState = entryState[propertyId];

        // The entry is not edited anymore.
        // The content is not edited, and should be `undefined` if the property was edited for the first time.
        // If the property was edited and saved a first time, then edited and cancelled, the content must stay to one from
        // the first edit.
        propertyState.editing = false;

      })

      this.editBus.$on('save-editing-entry', ({entryId, propertyId, content}) => {
        const entryState = this.editStates[entryId];
        const propertyState = entryState[propertyId];
        // The entry is not edited anymore but its content will need to be saved once the rest of the properties of the 
        // entry are not in edit mode. 
        propertyState.editing = false;
        propertyState.tosave = true;
        propertyState.content = content;
        this.save(entryId);
      })
    }

    save(entryId) {
      // Debounce with individual timeouts for each entry.
      if (this.editStatesTimeouts[entryId]) {
        clearTimeout(this.editStatesTimeouts[entryId])
      }
      // Wait 500ms to see if another entry is edited in the same row
      this.editStatesTimeouts[entryId] = setTimeout(() => this.checkSaveEntry(entryId), 500);
    }

    checkSaveEntry(entryId) {
      const values = this.editStates[entryId];
      var canBeSaved = true;
      for (const keyEntry in values) {
        const entryValue = values[keyEntry];

        const editing = entryValue.editing;
        const tosave = entryValue.tosave;
        canBeSaved = !editing && tosave;

        if (!canBeSaved) break;
      }

      if (canBeSaved) {
        // TODO: also we need to see how to handle the re-opening of an edited xclass property displayer, since we do 
        // not want to reload the form in this case, but instead redisplay the edited one.
        // Reset the edit state after save for a later edit round.

        // Aggregates the content of the form values of each property.
        const vals = {};
        for (const key in values) {
          values[key].content.forEach((content) => {
            this.aggregate(content, vals);
          })
        }

        this.logic.setValues({entryId, values: vals})
        this.editStates[entryId] = {};

      }
    }

    aggregate(content, vals) {
      for (const key in content) {
        const value = content[key];
        if (!vals[key]) {
          vals[key] = value;
        } else {
          if (!Array.isArray(vals[key])) {
            vals[key] = [vals[key]];
          }
          vals[key].push(value);
        }
      }
    }
  }

  function init(logic) {
    _logic = logic
    new EditBusService(_editBus, logic).init()
  }

  function start(entry, propertyId) {
    _editBus.$emit('start-editing-entry', {
      entryId: _logic.getEntryId(entry),
      propertyId
    });

  }

  function cancel(entry, propertyId) {
    _editBus.$emit('cancel-editing-entry', {
      entryId: _logic.getEntryId(entry),
      propertyId
    })

  }

  function save(entry, propertyId, content) {
    _editBus.$emit('save-editing-entry', {
      entryId: _logic.getEntryId(entry),
      propertyId: propertyId,
      content: content
    });
  }

  return {init, start, cancel, save};
})