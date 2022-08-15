<template>
  <div id="rendering-transformations-vue-root">
    <!-- TODO: Translations -->
    <h2>Macros Administration</h2>
    <!-- TODO: replace with an actual spinner. -->
    <hidden-macro-categories-form
      :default-hidden-categories="defaultHiddenCategories"
      :rest-path="restPath"
    />
    <macro-categories-form
      :rest-path="restPath"
      :macros-map="macrosMap"
    />
  </div>
</template>

<script>
import HiddenMacroCategoriesForm from "@/HiddenMacroCategoriesForm";
import MacroCategoriesForm from "@/MacroCategoriesForm";

export default {
  name: 'App',
  components: {MacroCategoriesForm, HiddenMacroCategoriesForm},
  props: {
    defaultHiddenCategories: {
      type: Array,
      required: true,
    },
    restPath: {
      type: String,
      required: true,
    },
    macrosMap: {
      type: Object,
      required: true
    }
  }
}
</script>

<style></style>
