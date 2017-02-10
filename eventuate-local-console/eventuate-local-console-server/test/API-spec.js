import chai  from 'chai';
import { expect } from 'chai';
import chaiHttp from 'chai-http';
chai.use(chaiHttp);

import helpers from './lib/helpers';
import WsClient from './lib/WsClient';
import util from 'util';

const webSocketUrl = helpers.webSocketUrl;

const baseUrl = `http://${process.env.DOCKER_HOST_IP}:3001/api`;

const timeout = 1000000;
let key;
let typeName;
let instanceId;

describe('API', function () {
  this.timeout(timeout);

  it('should GET /types', (done) => {
    chai.request(baseUrl)
    .get('/types')
    .end((err, res) => {
      if (err) {
        done(err);
      }

      expect(res).to.have.status(200);

      const { body } = res;
      console.log('body:', body);

      helpers.expectTypesResponse(body);
      typeName = body.items[0].name;
      done();
    });
  });

  it('should GET /instances/:typeName', (done) => {

    expect(typeName).to.be.ok;

    chai.request(baseUrl)
      .get(`/instances/${typeName}`)
      .end((err, res) => {
        if (err) {
          return done(err);
        }
        const { body } = res;
        console.log('body:', body);

        instanceId = body.items[0].aggregateId;
        done()
      });
  });

  it('should GET /instance/:typeName/:instanceId', (done) => {

    expect(instanceId).to.be.ok;

    chai.request(baseUrl)
      .get(`/instance/${typeName}/${instanceId}`)
      .end((err, res) => {
        if (err) {
          return done(err);
        }

        const { body } = res;
        console.log('body:', body);

        helpers.expectInstanceTypeNameInstanceIdResponse(body, done);
      });
  });

  it('should POST /api/subscription and subscribe', (done) => {

    const payload = [ 'net.chrisrichardson.eventstore.examples.todolist.backend.domain.TodoAggregate' ];

    chai.request(baseUrl)
      .post('/subscription')
      .send(payload)
      .end((err, res) => {

        if(err) {
          return done(err);
        }

        expect(res).to.have.status(200);
        const { body } = res;

        console.log('body:', body);

        helpers.expectSubscribeSuccessResponse(body, payload);

        key = body.key;
        done();
      });
  });

  it('should subscribe over WebSocket and receive event', (done) => {

    expect(key).to.be.ok;

    const wsClient = new WsClient({ webSocketUrl });
    wsClient.connect();

    wsClient.on('ready', () => {

      wsClient.sendMessage({
        type: 'WS_SERVER_COMMAND',
        payload: {
          data: {
            verb: 'subscribe',
            key,
            items: [ 'net.chrisrichardson.eventstore.examples.todolist.backend.domain.TodoAggregate' ]
          }
        }
      });

      helpers.createTodo()
        .then(result => {

        })
        .catch(done);
    });

    wsClient.on('message', message => {
      console.log('on message', message);
      helpers.expectEventSchema(JSON.parse(message.data));
      wsClient.close();
      done();
    });
  });

  it('should create many WS clients, subscribe, and receive events', (done) => {
    expect(key).to.be.ok;

    const CLIENTS_NUMBER = 10;
    const clients = helpers.createWsClients(CLIENTS_NUMBER);
    let receivedMessages = 0;
    const message = {
      type: 'WS_SERVER_COMMAND',
        payload: {
        data: {
          verb: 'subscribe',
            key,
            items: [ 'net.chrisrichardson.eventstore.examples.todolist.backend.domain.TodoAggregate' ]
        }
      }
    };
    
    clients.forEach(wsClient => {
      wsClient.connect();
      wsClient.on('ready', () => {
        console.log('client on ready');
        wsClient.sendMessage(message);
      });

      wsClient.on('message', (message) => {
        console.log('client on message', util.inspect(message, false, 5));
        helpers.expectEventSchema(JSON.parse(message.data));
        receivedMessages++;
        doneIfAllReceived();
      });
    });

    helpers.createTodo()
      .then(result => {

      })
      .catch(done);

    function doneIfAllReceived() {
      if (receivedMessages == CLIENTS_NUMBER) {
        helpers.closeWsClients(clients);
        done();
      }
    }
  });

  it('POST /api/subscription, should return error for empty topics array', (done) => {

    const payload = [ ];

    chai.request(baseUrl)
      .post('/subscription')
      .send(payload)
      .end((err, res) => {

        expect(res).to.have.status(400);
        const { body } = res;
        console.log('body:', body);
        helpers.expectSubscribeFailureResponse(body, done);
      });
  });
});
