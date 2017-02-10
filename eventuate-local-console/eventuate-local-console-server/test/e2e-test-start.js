// Reqires Node.js v4
'use strict';
require('./set-env');
const helpers = require('./lib/helpers');
// helpers.loadEnv();

require('../src/utils/checkEnv');


helpers.cloneTodoListApp()
  .then(cloned => {
    if (cloned) {
      return helpers.buildTodoListApp()
    } else {
      return Promise.resolve();
    }
  })
  .then(helpers.startServices)
  .then(() => {

    console.log('Services started');
    return helpers.prepareEventuateDB();
  })
  .then(helpers.nightwatchPrep)
  .then(helpers.nightwatch)
  .catch(err => {
    console.error('catch error:', err);
  });
