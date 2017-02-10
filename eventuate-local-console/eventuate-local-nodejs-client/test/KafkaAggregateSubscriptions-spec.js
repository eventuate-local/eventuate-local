import { getProducer, sendKafkaMessage, expectEnsureTopicExists } from './lib/helpers';
import KafkaAggregateSubscriptions from '../src/modules/KafkaAggregateSubscriptions';

const timeout = 25000;

const topic = 'io.eventuate.example.banking.domain.Account';

describe('KafkaAggregateSubscriptions', function () {

  this.timeout(timeout);

  it('should ensureTopicExistsBeforeSubscribing()', (done) => {

    const kafka = new KafkaAggregateSubscriptions();

    kafka.ensureTopicExistsBeforeSubscribing({ topics: [ topic ]})
      .then((result) => {

        console.log('result:' , result);
        expectEnsureTopicExists(result);
        done();
      })
      .catch(done);
  });

  it('should subscribe', (done) => {

    const subscriberId = 'KafkaAggregateSubscriptions-spec';
    const topics = [ topic ];

    const eventHandler = (event) => {

      return new Promise((resolve, reject) => {

        console.log('event:', event);

        resolve(event);
        done();
      });

    };

    const kafka = new KafkaAggregateSubscriptions();

    kafka.subscribe({ subscriberId, eventHandler, topics })
      .then((result) => {
        console.log('subscribe result', result);

      })
      .catch(done);

    getProducer()
      .then((producer) => {

        const message = `{ "entityType": "${topic}", "eventType": "io.eventuate.example.banking.domain.AccountCreated", "eventData": "{\\"initialBalance\\": \\"100\\"}", "id": "000001586e919e3qb-3e7acd0d2b4500045", "entityId": "000001586e919c0a-3e7acd0d2b450001", "swimlane": "0", "eventToken": "abcd" }`;

        sendKafkaMessage({ topic, message, producer })
          .then((result) => {
            console.log('Producer result: ', result);

          })
          .catch(done);
      })
      .catch(done);
  });
});