const {defineConfig} = require('@vue/cli-service');
module.exports = defineConfig({
  transpileDependencies: true,
  // Required to have a known "app.js" file in the WebJar, instead of a "random" hash.
  filenameHashing: false,
  chainWebpack: config => {
    'use strict';
    // Vue is imported from maven webpack dependencies.
    config.externals({
      "vue": "vue",
      "jquery": "jquery"
    });
  }
});
