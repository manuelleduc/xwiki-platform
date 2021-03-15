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
 The LivedataLayout component is used to display formatted data to the user.

 There are several layout, which are defined by their unique id.
 They should follow the path and naming convention:
 ./<layoutid>/Layout<layoutid>.vue

 The LivedataLayout component directly handle for us the choice of
 which layout to use, based on the Logic `currentLayoutId` property,
 and dynamically import this layout component and mount it at runtime.
 If the layout can't be found, or there was an loading error,
 then it falls back to the default one
 (specified by the `defaultLayout` property in the Livedata configuration).
-->
<template>
  <div class="livedata-layout">
    <!--
      We are using the <keep-alive> tag in order to keep the layout mounted
      even when it is not displayed on the screen.
      This is a sort of caching, that avoid re-rendering the whole layout
      each time we switch back on it.
    -->
    <keep-alive>
      <!-- This is where the specific filter component gets injected -->
      <component v-if="layoutComponent" :is="layoutComponent"></component>
    </keep-alive>
  </div>
</template>


<script>

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

      const editing = entryValue['editing'];
      const tosave = entryValue['tosave'];
      canBeSaved = !editing && tosave;

      if (!canBeSaved) break;
    }

    if (canBeSaved) {
      // TODO: also we need to see how to handle the re-opening of an edited xclass property displayer, since we do 
      // not want to reload the form in this case, but instead redisplay the edited one.
      // Reset the edit state after save for a later edit round.

      // Aggregates the content of the form values of each property.
      // const vals = this.aggregate(values);
      const vals = {};
      for (const key in values) {
        values[key].content.forEach((content) => {
          this.aggregate2(content, vals);
        })
      }

      // const vals = Object.entries(values)
      //     .reduce((acc, [key, val]) => {
      //       if (val.content) {
      //         acc[key] = val.content;
      //       }
      //       return acc
      //     }, {})
      this.logic.setValues({entryId, values: vals})
      this.editStates[entryId] = {};

    }
  }

  // aggregate(values) {
  //   const vals = {};
  //
  //   return vals;
  // }
  aggregate2(content, vals) {
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

export default {

  name: "LivedataLayout",

  inject: ["logic", "editBus"],

  created: function() {
    new EditBusService(this.editBus, this.logic).init()
  },

  props: {
    // The id of the layout to load
    layoutId: String,
  },

  data() {
    return {
      // The layout component
      // It is set to `undefined before it is resolved
      layoutComponent: undefined
    };
  },

  computed: {
    data() {
      return this.logic.data;
    },
  },


  // On mounted and when the `layoutId` prop change,
  // try to load the layout corresponding to the layoutId
  // or the default one as fallback
  watch: {
    layoutId: {
      immediate: true,
      handler() {
        // Try to load layout
        this.loadLayout(this.layoutId).catch(err => {
          // If the layout was not the default one, try to load default layout
          if (this.layoutId && this.layoutId !== this.data.meta.defaultLayout) {
            console.warn(err);
            this.logic.changeLayout(this.data.meta.defaultLayout);
          } else {
            console.error(err);
          }
        });
      },
    },
  },


  methods: {
    // Capitalize the given string
    capitalize(string) {
      string ??= "";
      return string[0].toUpperCase() + string.slice(1);
    },

    // Load the layout component corresponding to the given layoutId
    // On success, set `this.layoutComponent` to the retreived component,
    // which automatically insert the component in the html
    loadLayout(layoutId) {
      return new Promise((resolve, reject) => {

        layoutId ??= this.layoutId;

        // Load success callback
        const loadLayoutSuccess = layoutComponent => {
          this.layoutComponent = layoutComponent;
          resolve(layoutComponent);
        };

        // Load error callback
        const loadLayoutFailure = err => {
          reject(err);
        };

        // Load layout based on it's id
        import("./" + layoutId + "/Layout" + this.capitalize(layoutId) + ".vue")
            // We *have to* destructure the return value as `{ default: component }`,
            // because it's how Webpack is handling dynamic imports
            .then(({default: component}) => loadLayoutSuccess(component))
            .catch(err => void loadLayoutFailure(err));
      });
    },
  },

};
</script>


<style>

</style>
