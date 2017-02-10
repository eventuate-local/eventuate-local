'use strict';

const fs = require('fs');
const simpleGit = require('simple-git')();
const spawn = require('child_process').spawn;
const path = require('path');

const env = require('node-env-file');


const chai = require('chai');
const expect = chai.expect;
const chaiHttp = require('chai-http');
chai.use(chaiHttp);

const todoListRepoPath = 'https://github.com/eventuate-examples/eventuate-examples-java-spring-todo-list.git';
const todoListLocalDir = 'test/res/todo-list-app';
const todoListAppUrl = `http://${process.env.DOCKER_HOST_IP}:8080`;
const whaitTodoListAppDelay = 90000;

const webSocketUrl = `http://${process.env.DOCKER_HOST_IP}:3001/comm`;
module.exports.webSocketUrl = webSocketUrl;

module.exports.startServices = function () {
  return Promise.all([
    startTodoListApp(),
    startEventuateConsole()
  ]);
  // return startTodoListApp().then(startEventuateConsole)
};

module.exports.stopServices = function () {
  return Promise.all([
    stopTodoListApp(),
    stopEventuateConsole()
  ]);
  // return stopTodoListApp().then(stopEventuateConsole)
};


module.exports.cloneTodoListApp = function () {
  return new Promise((resolve, reject) => {
  
    fs.stat('./' + todoListLocalDir, (err, stats) => {
    
      if(!err) {
        console.log(`Directory '${todoListLocalDir}' exists`);
        return resolve(false);
      }
  
      //Clone Todo List repo if directory not exists
      if (err.code == 'ENOENT') {
      
        simpleGit.clone(todoListRepoPath, todoListLocalDir, (err, res) => {
          if (err) {
            return reject(err);
          }
          
          console.log(res);
          
          resolve(true);
          
        });
      } else {
        reject(err);
      }
    })
  
  }); 

};

module.exports.prepareEventuateDB = function () {
  console.log('Prepare Eventuate database');
  
  return promisedDelay(whaitTodoListAppDelay).then(createTodo).then(deleteTodo);
};

function createTodo() {
  console.log('Create todo');
  
  const todo = {
    completed: false,
    order: 0,
    title: "Test#1"
  };

  return chai.request(todoListAppUrl)
    .post('/todos')
    .send(todo)
    .then(res => {
      console.log('res.body:', res.body);
      return Promise.resolve(res);
    })
    .catch(err => {
      console.log('err', err);
      return Promise.reject(err);
    });
}

module.exports.createTodo = createTodo;

function deleteTodo() {
  console.log('Delete todos');
  
  return chai.request(todoListAppUrl)
    .delete('/todos')
    .then(res => {
      console.log('res.body:', res.body);
      return Promise.resolve(res);
    })
    .catch(err => {
      console.log('err', err);
      return Promise.reject(err);
    });
}

module.exports.buildTodoListApp = function () {
  const path = `${todoListLocalDir}/single-module`;
  
  return runCommand('./gradlew', [ 'assemble', '-P', 'eventuateDriver=local' ], path)
    .then(runCommand.bind(this, 'docker-compose', [ '-f', 'docker-compose-eventuate-local.yml', 'build' ], path));
};

module.exports.cleanup = function(){
  // docker rmi $(docker images -f "dangling=true" -q)
  return runCommand('docker', ['images', '-f', 'dangling=true', '-q'], '.')
    .then(function(data) {
      return Promise.all(data.split(/\w+/).map(function(cont){
        if (!cont) {
          return Promise.resolve();
        }
        return runCommand('docker', ['rmi', cont], '.');
      }));
      // return runCommand('docker', ['rmi', ], '.');
    });

  // return runCommand('docker $(docker images -f "dangling=true" -q)', [], '.');
};

module.exports.nightwatchPrep = function() {
  // node_modules/selenium-standalone/bin/selenium-standalone install
  return runCommand('node', ['./node_modules/selenium-standalone/bin/selenium-standalone', 'install'], '.')
};

module.exports.nightwatch = function() {
  return runCommand('node', ['./node_modules/.bin/nightwatch',
    'test/e2e-tests/test010_Initial.js',
    'test/e2e-tests/test020_Subscribe.js',
    'test/e2e-tests/test030_EventsList.js'], '.')
};

function startTodoListApp() {
  const path = `${todoListLocalDir}/single-module`;
  return runCommand('docker-compose', [ '-f', 'docker-compose-eventuate-local.yml', 'rm', '--force' ], path)
    .then(runCommand.bind(this, 'docker-compose', [ '-f', 'docker-compose-eventuate-local.yml', 'up', '-d' ], path));
}

function stopTodoListApp() {
  return  runCommand('docker-compose', [ '-f', 'docker-compose-eventuate-local.yml', 'stop' ], `${todoListLocalDir}/single-module`);
}

function startEventuateConsole() {
  return runCommand('./docker-compose.sh', [ 'up', '-d' ], './docker');
}

function stopEventuateConsole() {
  return runCommand('docker-compose', [ 'stop' ], './docker');
}

function runCommand(command, params, path) {

  console.log(`Run command: ${command}`);
  console.log(`params: ${ params.join(' ') }`);
  console.log(`path: ${path}`);
  console.log('');

  let memo = [];

  return new Promise((resolve, reject) => {
     
    const proc = spawn(command, params, { cwd: path });
    
    proc.stdout.on('data', (data) => {
      console.log(`${data}`);
      memo.push(data);
    });

    proc.stderr.on('data', (data) => {
      console.log(`stderr: ${data}`);
    });

    proc.on('close', (code) => {
      console.log(`Process exited with code: ${ code }`);
      if (code === 0) {
        resolve(memo);
      } else {
        reject(code);
      }
      memo = null;
    });
  });
}

function promisedDelay(timeout) {
  console.log(`promisedDelay: ${timeout}`);
  return new Promise(resolve => {
    setTimeout(resolve, timeout);
  });
}

module.exports.promisedDelay = promisedDelay;

module.exports.expectTypesResponse = (body) => {
  expect(body).to.be.an('Object');
  expect(body).to.hasOwnProperty('items');
  expect(body.items).to.be.an('Array');
  expect(body.items).to.be.not.empty;

  const type = body.items[0];
  expect(type).to.be.an('Object');
  expect(type).to.hasOwnProperty('name');
  expect(type.name).to.be.a('String');
};

module.exports.expectInstancesTypeNameResponse = (body) => {
  expect(body).to.be.an('Object');
  expect(body).to.have.ownProperty('limit');
  expect(body.limit).to.be.a('Number');

  expect(body).to.have.ownProperty('offset');
  expect(body.limit).to.be.a('Number');

  expect(body).to.have.ownProperty('items');
  expect(body.items).to.be.an('Array');
  expect(body.items).to.be.not.empty;

  const item = body.items[0];
  expect(item).to.be.an('Object');
  expect(item).to.have.ownProperty('aggregateId');
  expect(item.aggregateId).to.be.a('String');

  expect(item).to.have.ownProperty('type');
  expect(item.type).to.be.a('String');
  expect(item).to.have.ownProperty('entity_version');
  expect(item.entity_version).to.be.a('String');
  expect(item).to.have.ownProperty('created');
  expect(item.created).to.be.a('Number');
  expect(item).to.have.ownProperty('updated');
  expect(item.updated).to.be.a('Number');
  expect(item).to.have.ownProperty('events');
  expect(item.updated).to.be.a('Number');
};

module.exports.expectInstanceTypeNameInstanceIdResponse = (body, done) => {
  expect(body).to.have.ownProperty('items');
  expect(body.items).to.be.an('Array');
  expect(body.items).to.be.not.empty;

  const item = body.items[0];
  expectEventSchema(item);

  if (typeof done == 'function') {
    done();
  }
};

function expectEventSchema(event) {
  expect(event).to.be.an('Object');
  expect(event).to.have.ownProperty('eventId');
  expect(event.eventId).to.be.a('String');
  expect(event).to.have.ownProperty('eventType');
  expect(event.eventType).to.be.a('String');
  expect(event).to.have.ownProperty('eventData');
  expect(event.eventData).to.be.an('Object');
}

module.exports.expectEventSchema = expectEventSchema;

module.exports.expectSubscribeSuccessResponse = (body, payload, done) => {
  expect(body).to.be.an('Object');
  expect(body).to.haveOwnProperty('key');
  expect(body).to.haveOwnProperty('items');
  expect(body.items).to.be.an('Array');
  expect(body.items).eql(payload);

  if (typeof done == 'function') {
    done();
  }
};

module.exports.expectSubscribeFailureResponse = (body, done) => {
  expect(body).to.be.an('Object');
  expect(body).to.haveOwnProperty('err');

  if (typeof done == 'function') {
    done();
  }
};


module.exports.createWsClients = (number) => {

  const WsClient = require('./WsClient');

  const clients = [];

  for(let i = 0; i < number; i++) {
    clients.push(new WsClient({ webSocketUrl }));
  }

  return clients;

};

module.exports.closeWsClients = (clients) => {
  clients.forEach(client => client.close());
};
