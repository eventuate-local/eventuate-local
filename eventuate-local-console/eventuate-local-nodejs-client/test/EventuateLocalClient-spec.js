import { getProducer, sendKafkaMessage, expectAfterSubscribe, expectAfterUnSubscribe, expectEvent } from './lib/helpers';
import EventuateLocalClient from '../src/modules/EventuateLocalClient';
import { expect } from 'chai';

if(!process.env.EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING) {
  throw new Error('EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING is not set');
}

const timeout = 250000;

const eventuateClient = new EventuateLocalClient();


describe('EventuateLocalClient subscribe()', function () {

  this.timeout(timeout);

  it('should subscribe using topics array', (done) => {

    const subscriberId = 'subscriberId1';
    const topic = 'io.eventuate.example.banking.domain.Account1';
    const topics = [ topic ];
    const message = `{ "entityType": "${topic}", "eventType": "io.eventuate.example.banking.domain.AccountCreated", "eventData": "{\\"initialBalance\\": \\"100\\"}", "id": "000001586e919e3qb-3e7acd0d2b4500045", "entityId": "000001586e919c0a-3e7acd0d2b450001", "swimlane": "0", "eventToken": "abcd" }`;

    sendMessage(topic, message, done);

    const eventHandler = (event) => {

      return new Promise((resolve, reject) => {

        console.log('event:', event);

        expectEvent(event);

        expect(event.entityType).to.equal(topic);

        resolve(event);
        done();
      });

    };

    eventuateClient.subscribe(subscriberId, topics, eventHandler, createCallback(done))
      .then((result) => {
        console.log('subscribed');
        // console.log('subscribe result', result);
        expectAfterSubscribe(eventuateClient, subscriberId);
      })
      .catch(err => {
        console.error('Subscribe error: ', err);
      });
  });

  it('should subscribe using entityTypesAndEvents object', (done) => {

    const subscriberId = 'subscriberId2';
    const topic = 'io.eventuate.example.banking.domain.Account2';
    const entityTypesAndEvents = {
      [topic]: [
        'io.eventuate.example.banking.domain.AccountCreated'
      ]
    };

    const message = `{ "entityType": "${topic}", "eventType": "io.eventuate.example.banking.domain.AccountCreated", "eventData": "{\\"initialBalance\\": \\"100\\"}", "id": "000001586e919e3qb-3e7acd0d2b4500045", "entityId": "000001586e919c0a-3e7acd0d2b450001", "swimlane": "0", "eventToken": "abcd" }`;

    sendMessage(topic, message, done);


    const eventHandler = (event) => {

      return new Promise((resolve, reject) => {

        console.log('event:', event);

        expectEvent(event);

        resolve(event);
        done();
      });

    };


    eventuateClient.subscribe(subscriberId, entityTypesAndEvents, eventHandler, createCallback(done))
      .then((result) => {
        console.log('subscribed');
        // console.log('subscribe result', result);
        expectAfterSubscribe(eventuateClient, subscriberId);

      })
      .catch(done);

  });


  it('should subscribe and unSubscribe', (done) => {

    const subscriberId = 'subscriberId3';

    const eventHandler = (event) => {

      setTimeout(() => {

        eventuateClient.unSubscribe({ subscriberId })
          .then(() => {
            console.log('un subscribed');
            expectAfterUnSubscribe(eventuateClient, subscriberId);
            done();
          })
          .catch(done);


      }, 3000);
      return Promise.resolve(event);
    };

    const topic = 'io.eventuate.example.banking.domain.Account3';
    const topics = [ topic ];

    eventuateClient.subscribe(subscriberId, topics, eventHandler, createCallback(done))
      .then((result) => {
        console.log('subscribed');
        console.log(eventuateClient);
        expectAfterSubscribe(eventuateClient, subscriberId);
      })
      .catch(done);

    const message = `{ "entityType": "${topic}", "eventType": "io.eventuate.example.banking.domain.AccountCreated", "eventData": "{\\"initialBalance\\": \\"100\\"}", "id": "000001586e919e3qb-3e7acd0d2b4500045", "entityId": "000001586e919c0a-3e7acd0d2b450001", "swimlane": "0", "eventToken": "abcd" }`;

    sendMessage(topic, message, done);

  });
});

function sendMessage(topic, message, done) {
  return getProducer()
    .then((producer) => {

      sendKafkaMessage({ topic, message, producer })
        .then((result) => {
          console.log('Producer result: ', result);

        })
        .catch(done);
    })
    .catch(done);
}

function createEventHandler(done) {

  return (event) => {

    return new Promise((resolve, reject) => {

      console.log('event:', event);

      expectEvent(event);

      resolve(event);
      done();
    });

  };
}

function createCallback(done) {
  return (err, result) => {

    if (err) {
      return done(err);
    }

    console.log('subscribe callback invoked');
    // console.log(`callback result:`, result);
  }
}
