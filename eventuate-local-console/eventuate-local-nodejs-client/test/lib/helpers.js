import { Producer, Client, ConsumerGroup } from 'kafka-node';
import { expect } from 'chai';

const client = new Client(process.env.EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING);

client.on('ready', () => {
  console.log('client on ready')
});

client.on('error', (err) => {
  console.error('client on error', err);
});

client.on('close', (err) => {
  console.log('client on close');
});

client.on('connect', (err) => {
  console.log('client on connect');
});

let producer;

export const getProducer = () => {

  return new Promise((resolve, reject) => {

    if (!producer) {

      let p = new Producer(client, { requireAcks: 1 });

      console.log('producer:' ,producer);
      p.on('ready', () => {
        console.log('producer on ready');
        producer = p;
        resolve(producer);
      });

      p.on('error', (err) => {
        reject(err);
      });

      p.on('close', () => {
        console.log('Producer closed');
      });
    } else {

      resolve(producer);
    }


  })
};

export const sendKafkaMessage = ({ topic, message, producer}) => {

  return new Promise((resolve, reject) => {

    const topics = [
      { topic, partition: 0, messages: [ message ], attributes: 0 }
    ];

    console.log('topics:', topics);

    producer.send(topics, (err, result) => {

      if (err) {
        return reject(err);
      }

      resolve(result);
    });
  });

};

export const expectEnsureTopicExists = (res) => {

  expect(res).to.be.an('Array');
  expect(res).lengthOf(2);
  expect(res[0]).to.be.an('Object');

  expect(res[0]).to.haveOwnProperty('0');

  expect(res[0]['0']).to.be.an('Object');
  expect(res[0]['0']).to.haveOwnProperty('nodeId');
  expect(res[0]['0']).to.haveOwnProperty('host');
  expect(res[0]['0']).to.haveOwnProperty('port');

  expect(res[1]).to.haveOwnProperty('metadata');
  expect(res[1]['metadata']).to.be.an('Object');

};

export const expectAfterSubscribe = (eventuateClient, subscriberId) => {

  expect(eventuateClient.subscriptions).to.be.an('Object');
  expect(eventuateClient.subscriptions).to.haveOwnProperty(subscriberId);
  expect(eventuateClient.subscriptions[subscriberId]).to.be.instanceOf(ConsumerGroup);
};

export const expectAfterUnSubscribe = (eventuateClient, subscriberId) => {

  expect(eventuateClient.subscriptions).to.be.an('Object');
  expect(eventuateClient.subscriptions).to.not.haveOwnProperty(subscriberId);
};

export const expectEvent = (event) => {

  expect(event).to.be.an('Object');

  expect(event).to.have.property('eventId');
  expect(event.eventId).to.be.a('String');
  expect(event.eventId).to.be.not.empty;

  expect(event).to.have.property('entityId');
  expect(event.entityId).to.be.a('String');
  expect(event.entityId).to.be.not.empty;

  expect(event).to.have.property('eventType');
  expect(event.eventType).to.be.a('String');
  expect(event.eventType).to.be.not.empty;

  expect(event).to.have.property('eventData');
  expect(event.eventData).to.be.an('Object');
  expect(event.eventData).to.be.not.empty;

  expect(event).to.have.property('eventToken');
  expect(event.eventToken).to.be.an('String');
  expect(event.eventToken).to.be.not.empty;

  expect(event).to.have.property('entityType');
  expect(event.entityType).to.be.an('String');
  expect(event.entityType).to.be.not.empty;

  expect(event).to.have.property('swimlane');
  expect(event.swimlane).to.be.an('String');
  expect(event.swimlane).to.be.not.empty;

};
