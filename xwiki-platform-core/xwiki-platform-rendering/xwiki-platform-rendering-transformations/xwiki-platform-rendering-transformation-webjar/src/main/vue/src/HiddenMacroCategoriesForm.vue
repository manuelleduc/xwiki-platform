<template>
  <form class="xform">
    <h3>Macro Hidden Categories</h3>
    <dl>
      <dt>
        <!-- TODO: Translations -->
        <label for="hiddenMacroCategories">
          Macro Hidden Categories
        </label>
        <span class="xHint">
            Hidden by default categories. Users without "show hidden pages" set to true do not see those macros.
          </span>
      </dt>
      <dd>
        <select
          id="hiddenMacroCategories"
          ref="hiddenMacroCategories"
          multiple
        >
          <option
            v-for="category in defaultHiddenCategories"
            :key="category"
            :value="category"
            selected
          >
            {{ category }}
          </option>
        </select>
      </dd>
    </dl>
    <div class="bottombuttons">
      <p>
        <span class="buttonwrapper">
          <!-- TODO: localization. -->
          <input
            type="submit"
            class="button"
            value="Save"
            ref="saveHiddenMacroCategories"
            @click="saveHiddenMacroCategories"
          />
        </span>
      </p>
    </div>
  </form>
</template>

<script>
import $ from "jquery";

export default {
  name: "HiddenMacroCategoriesForm",
  props: {
    defaultHiddenCategories: {
      type: Array,
      required: true,
    },
    restPath: {
      type: String,
      required: true,
    }
  },
  methods: {
    saveHiddenMacroCategories() {
      // TODO: test that hiddenMacroCategoriesSelectize is defined and that getValue() returns an array.
      // TODO: find which endpoint to use to save the values.
      // TODO: when endpoint found, also check what it returns.
      console.log('saveHiddenMacroCategories', this.$refs.hiddenMacroCategories.selectize.getValue());
      const spaces = ["Rendering", "Transformation", "Code"];
      const page = "Configuration";
      const className = "Rendering.Transformation.Code.ConfigurationClass";
      const spacesPath = spaces.map((space) => "/spaces/" + space).join("");
      // TODO: translations
      const saveNotification = new XWiki.widgets.Notification("Saving Macro hidden categories...", "inprogress");
      $.ajax(`${this.restPath}${spacesPath}/pages/${page}/objects/${className}/0/properties/hiddenMacroCategories`, {
        type: "PUT",
        dataType: "json",
        Accept: "application/json",
        contentType: "application/json",
        // data: JSON.stringify({value: this.$refs.hiddenMacroCategories.selectize.getValue().join(",")}),
        data: {value: this.$refs.hiddenMacroCategories.selectize.getValue().join(",")},
      })
        .done(() => {
          saveNotification.replace(new XWiki.widgets.Notification("Macro hidden categories saved.", "done"));
        })
        .fail(() => {
          saveNotification.replace(new XWiki.widgets.Notification("Macro hidden categories save fail.", "error"));
        });
    }
  },
  mounted() {
    this.$nextTick(() => {
      $(this.$refs.hiddenMacroCategories).xwikiSelectize({
        create: true
      });
    })
  }

}
</script>

<style scoped>

</style>
