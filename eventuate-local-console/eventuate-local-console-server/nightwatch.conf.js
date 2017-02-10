require('babel-core/register');
var fs = require('fs');
var path = require('path');

var merge = require('object-merge');

function wrap(obj) {
  console.log(JSON.stringify(obj, null, '  '));
  return obj;
}

module.exports = wrap(merge(require('./nightwatch.json'), {
  'test_settings': {
    'default': {
      'launch_url': 'http://' + (process.env.DOCKER_HOST_IP || process.env.SERVICE_HOST || 'localhost') + ':' + (process.env.SERVICE_PORT || '8080')
    }
  },
  'selenium': {
    'cli_args': {
      'webdriver.chrome.driver': correctExecutablePath('chromedriver'),
      'webdriver.firefox.driver': correctExecutablePath('geckodriver'),
      'webdriver.gecko.driver': correctExecutablePath('geckodriver')
    }
  }
}));

function correctExecutablePath(key) {
  console.log(path.resolve('.'));
  try {
    var files = fs.readdirSync(`./node_modules/selenium-standalone/.selenium/${ key }`);

    var selectedExecFile = files.filter(fName => !/(\.zip)|(\.gz)$/i.test(fName));
    if (selectedExecFile.length !== 1) {
      return '';
    }

    var result = `./node_modules/selenium-standalone/.selenium/${ key }/${ selectedExecFile[0] }`;
    console.log(result);

    return result;
  } catch (ex) {
    return '';
  }

}