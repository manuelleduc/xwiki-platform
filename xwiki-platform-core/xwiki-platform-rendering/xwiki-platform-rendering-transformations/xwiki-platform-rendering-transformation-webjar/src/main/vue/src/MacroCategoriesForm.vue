<template>
  <div v-if="macroDefinitions === undefined">LOADING</div>
  <div v-else>
    <h3>Macro Categories</h3>
    <div class="xform">
      <dl>
        <dt>
          <label for="newMacroConfiguration">
            Define a new macro configuration
          </label>
        </dt>
        <dd>
          <select id="newMacroConfiguration" ref="newMacroConfiguration">
            <option value=""></option>
            <option v-for="macroId in undefinedMacroIds" :key="macroId" :value="macroId">
              {{ macroId }}
            </option>
          </select>
        </dd>
        <dt v-show="newMacroConfigId !== ''">
          <label for="newMacroCategories">
            Define the new categories of {{ newMacroConfigId }}.
          </label>
        </dt>
        <dd v-show="newMacroConfigId !== ''">
          <select id="newMacroCategories" ref="newMacroCategories" multiple>
            <!-- TODO: find out why selected is not computed with the right category! -->
            <option
              v-for="category in allMacroCategories"
              :value="category"
              :key="`${category}${newMacroConfigId}`"
              :selected="hasMacroCategory(newMacroConfigId, category)"
            >
              {{ category }}
              }})
            </option>
          </select>
        </dd>
      </dl>
    </div>
    <div>
      <div v-if="macroDefinitions.length === 0">(empty)</div>
      <div v-else>
        <div v-for="macro in macroDefinitions" :key="macro.id">
          {{ macro }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import $ from "jquery";

export default {
  name: "MacroCategoriesForm",
  props: {
    restPath: {
      type: String,
      required: true,
    },
    macrosMap: {
      type: Object,
      required: true,
    }
  },
  data() {
    return {
      macroDefinitions: undefined,
      newMacroConfigId: ''
    }
  },
  methods: {
    hasMacroCategory(newMacroConfigId, category) {
      return this.getMacroCategories(newMacroConfigId).includes(category)
    },
    getAllObjects() {
      return new Promise((resolve, reject) => {
        const className = "Rendering.Transformation.Code.MacroOverrideClass";
        $.getJSON(`${this.restPath}/classes/${className}/objects`)
          .success(data => resolve(data))
          .fail(() => {
            // TODO: handle error better!!
            reject();
          });
      })
    },
    initializeSelectize() {
      this.$nextTick(() => {
        $(this.$refs.newMacroConfiguration).xwikiSelectize({
          onChange: (value) => {
            this.newMacroConfigId = value;
          }
        });

        $(this.$refs.newMacroCategories).xwikiSelectize({
          create: true
        });
      })
    },
    getMacroCategories(macroId) {
      console.log('>>>>', macroId, this.macrosMap, this.macrosMap[macroId], this.macrosMap[macroId] || []);
      return this.macrosMap[macroId] || [];
    }
  },
  computed: {
    macroIds() {
      return Object.keys(this.macrosMap);
    },
    allMacroCategories() {
      // The list of unique categories
      return [...new Set(this.macroIds.map(this.getMacroCategories).flat())];
    },
    undefinedMacroIds() {
      if (this.macroDefinitions === undefined) {
        return [];
      } else {
        return this.macroIds.filter(macroId => {
          return this.macroDefinitions.find(macro => macro.id === macroId) === undefined;
        });
      }
    }
  },
  created() {
    this.getAllObjects()
      .then(data => {
        this.macroDefinitions = data.objectSummaries;
        // Must be done after the macro definition so that the options are rendered.
        this.initializeSelectize();
      })
      .catch(() => {
        new XWiki.widgets.Notification("Macro definitions load error.", "error");
      });
  }
}
</script>

<style scoped>

</style>
