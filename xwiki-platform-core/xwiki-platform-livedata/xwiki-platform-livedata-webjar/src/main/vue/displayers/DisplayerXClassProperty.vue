<!--
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
-->


<!--
  DisplayerXClassProperty is a custom displayer that displays an XClass property.
  It fetches the edit and view widgets from the server.
-->
<template>
  <!-- Uses the BaseDisplayer as root element, as it handles for us all the displayer default behavior. -->
  <BaseDisplayer
      class="displayer-xclass-property"
      :property-id="propertyId"
      :entry="entry"
      :is-view.sync="isView"
      :is-loading="isLoading"
      :is-editing="editField !== undefined"
      @saveEdit="applyEdit">

    <!-- Provide the Html Viewer widget to the `viewer` slot -->
    <template #viewer>
      <div :class="[html-wrapper, isLoading ? disabled : '']" v-html="viewField"></div>
    </template>

    <!-- Provide the Html Editor widget to the `editor` slot -->
    <template #editor>
      <!-- TODO: we must keep the div alive to avoid re-render with the initial content of `editField` each time, and 
           consequently loose all the edits done previously. This is particularly useful in the case where the user 
           goes back the edit again an xclass field before the entry has been saved.
       -->
      <div v-html="editField" ref="xClassPropertyEdit"></div>
    </template>
  </BaseDisplayer>
</template>


<script>

import displayerMixin from "./displayerMixin.js";
import BaseDisplayer from "./BaseDisplayer.vue";
import displayerStatesMixin from "./displayerStatesMixin.js";
import $ from "jquery";


export default {

  name: "displayer-xlass-property",

  inject: ["logic", "xClassPropertyHelper"],

  // Add the displayerMixin to get access to all the displayers methods and computed properties inside this component.
  mixins: [displayerMixin, displayerStatesMixin],

  props: ['timestamp'],

  components: {BaseDisplayer,},

  data() {
    return {
      editField: undefined,
      viewField: undefined,
    }
  },

  methods: {

    applyEdit() {
      const documentName = this.entry["doc.fullName"];
      if (!documentName) {
        // TODO: translations
        new XWiki.widgets.Notification("Can't save an XClass property with a missing document name.", 'error');
      } else {
        document.fire('xwiki:actions:beforeSave');
        const content = $(this.$refs.xClassPropertyEdit).find(':input').serializeArray();
        const className = this.data.query.source.className;

        const newContents = [];
        for (const key in content) {
          if (content.hasOwnProperty(key)) {
            const value = content[key];
            if (value['name']) {
              var newName = value['name'];

              if (newName.startsWith(className)) {
                newName = newName.replace(className, '');
              }

              newName = newName.replace(/_\d+_/, '');
              newContents.push({[newName]: value['value']})
            }
          }
        }

        this.editBus.save(this.entry, this.propertyId, newContents)
      }
    },

    /**
     * Takes an update method retrieve its content.
     * @param {method} updateMethod the method dedicate to the update of a given aspect of the displayer. For instance,
     *  the view or edit html content
     * @returns {*} a `Promise` with the content of the updated view
     */
    update(updateMethod) {
      this.isLoading = true;
      const documentName = this.entry["doc.fullName"];
      const className = this.data.query.source.className;
      const property = this.propertyId;
      return updateMethod(documentName, className, property);
    },

    /**
     * Updates the content of the viewer slot.
     */
    updateView() {
      this.update(this.xClassPropertyHelper.view)
          .then((html) => {
            this.isLoading = false;
            this.viewField = html;
            // Allow others to enhance the viewer.
            $(document).trigger('xwiki:dom:updated', {'elements': [this.$el]});
          })
          .catch(() => {
            // Stop the loader and switch to view mode. 
            this.isLoading = false;
          })
    },

    /**
     * Update the content of the editor slot.
     */
    updateEdit(forced) {
      // Reload only if the edit field has not been already initialized, unless the update if forced.
      if (this.editField === undefined || forced) {
        this.update(this.xClassPropertyHelper.edit)
            .then((html) => {
              this.isLoading = false;

              this.editField = html;
              // Allow others to enhance the viewer.
              $(document).trigger('xwiki:dom:updated', {'elements': [this.$el]});
            })
            .catch(() => {
              // Stop the loader and switch to view mode. 
              this.isLoading = false;
              this.isView = true;
            })
      }
    },
    refreshXClassProperty({isView, forced}) {
      if (!isView) {
        // Updates the edit form when passing edit mode.
        this.updateEdit(forced);
      } else {
        // Updates the view form when passing in view mode.
        this.updateView();
      }
    }
  },

  watch: {
    isView: function(isView) {
      this.refreshXClassProperty({isView, forced: false})
    },
    timestamp: function(timestamp) {
      // Reset the edit field and force the reload of the view field when the component is re-rendered.
      this.editField = undefined
      this.refreshXClassProperty({isView: this.isView, forced: true});
    }
  },
  mounted() {
    if (!this.viewField) {
      this.updateView();
    }
  }
};

</script>


<style>

</style>
