//Reqires Node.js v4
'use strict';
require('./set-env');
const helpers = require('./lib/helpers');
// helpers.loadEnv();

require('../src/utils/checkEnv');


Promise.resolve()
  .then(helpers.stopServices)
  .then(helpers.cleanup)
  .then(() => {
    console.log('Services stopped');
  });
