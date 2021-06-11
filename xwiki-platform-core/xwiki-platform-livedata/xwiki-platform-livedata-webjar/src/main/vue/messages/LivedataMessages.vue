<template>
  <div v-if="translationLoaded">
    <div v-for="(message, index) in messages" :key="`message-${message.translationKey}`"
         :class="['box', `${message.level}message`]">
      (<small>{{ index + 1 }}</small>) {{ $t(message.translationKey) }}
    </div>
  </div>
</template>

<script>
export default {
  name: "LivedataMessages",
  inject: ["logic"],
  data() {
    return {
      translationLoaded: false
    }
  },
  computed: {
    messages() {
      return this.logic.data.data.messages;
    }
  },
  watch: {
    messages: {
      immediate: true,
      handler(newMessages) {
        if (newMessages) {
          this.logic.loadTranslations({
            prefix: '',
            keys: newMessages.map(message => message.translationKey)
          }).then(() => this.translationLoaded = true)
        }
      }
    }
  }
}
</script>

<style scoped>

</style>