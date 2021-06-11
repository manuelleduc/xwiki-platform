<template>
  <span>
    <sup v-for="index in messageIndexes" :key="`message-indexes-${index}`">
      {{ index }}
    </sup>
  </span>
</template>

<script>
export default {
  name: "MessageIndexes",
  props: {
    messageIndexes: {
      required: true
    }
  }
}
</script>

<style scoped>

</style>