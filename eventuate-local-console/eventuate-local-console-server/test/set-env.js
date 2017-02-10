/**
 * Created by andrew on 1/31/17.
 */
const fs = require('fs');
const simpleGit = require('simple-git')();
const spawn = require('child_process').spawn;
const path = require('path');
const env = require('node-env-file');

module.exports.loadEnv = () => {
  const envFile = path.join(__dirname, '..', '.env');

  try {

    fs.statSync(envFile);

    env(envFile, {
      verbose: true,
      raise: false,
      logger: console,
      overwrite: true
    });
  } catch (err) {

  }
}

module.exports.loadEnv();