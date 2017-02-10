// Reqires Node.js v4
'use strict';
require('./set-env');
const helpers = require('./lib/helpers');
// helpers.loadEnv();

require('../src/utils/checkEnv');


Promise.resolve()
  .then(helpers.nightwatchPrep)
  .then(helpers.nightwatch)
  // .then(helpers.promisedDelay.bind(this, 1000000000))
  .catch(err => {
    console.error('catch error:', err);
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



