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
  //.then(helpers.cleanup)
  .then(() => {

    console.log('Services started');

    return helpers.prepareEventuateDB();
  })
  .then(helpers.nightwatchPrep)
  .then(helpers.nightwatch)
  // .then(helpers.promisedDelay.bind(this, 1000000000))
  .catch(err => {
    console.error('catch error:', err);
  })
  .then(helpers.stopServices)
  //.then(helpers.cleanup)
  .then(() => {
    console.log('Services stopped');
  });
  
process.on('SIGINT', function() {

  console.log("\nCaught interrupt signal");

  helpers.stopServices()
    .then(process.exit.bind(this, 0))
    .catch(err => {
      console.error(err);
      process.exit(1)
    });
});



