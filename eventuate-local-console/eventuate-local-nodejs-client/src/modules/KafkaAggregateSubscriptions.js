import { ConsumerGroup, Offset, Client } from 'kafka-node';
import isPortReachable from 'is-port-reachable';
import { getLogger } from './logger';
import { makeEvent } from './utils';
import util from 'util';

const logger = getLogger({ title: 'KafkaAggregateSubscriptions' });

export default class KafkaAggregateSubscriptions {

  constructor({ connectionString = '', fromOffset = 'earliest' } = {}) {
    this.connectionString = connectionString || process.env.EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING;
    this.fromOffset = fromOffset;

    this.consumerGroupDefaultOptions = {
      host: this.connectionString,
      sessionTimeout: 30000,
      autoCommit: true,// TODO: should be false
      fromOffset,
    };

    this.connectionTimeout = 5000;
  }

  subscribe({ subscriberId, topics, eventHandler }) {

    return this.checkKafkaIsReachable()
      .then(reachable => {

        logger.debug('reachable:', reachable);

        if (!reachable) {
          return Promise.reject(new Error(`Host unreachable ${this.connectionString}`))
        }

        return this.ensureTopicExistsBeforeSubscribing({ topics })
          .then(() => {
            return this.createConsumerGroup({ groupId: subscriberId, topics, eventHandler });
          })
      })
  }

  ensureTopicExistsBeforeSubscribing({ topics }) {

    const client = new Client(this.connectionString);

    return new Promise((resolve, reject) => {

      client.on('ready', () => {

        logger.debug('client on ready');

        client.loadMetadataForTopics(topics, (err, resp) => {

          if (err) {
            return reject(err);
          }

          logger.debug('loadMetadataForTopics resp:', JSON.stringify(resp));

          for(let topic in resp[1].metadata) {

            const partitions = Object.keys(resp[1].metadata[topic])
              .map(index => resp[1].metadata[topic][index])
              .map(metadata => {
                return metadata.partition;
              });

            logger.debug(`Got these partitions for Topic ${topic}: ${partitions.join(', ')}`);
          }

          resolve(resp);
        });

      });

      client.on('error', reject);

    });
  }

  checkKafkaIsReachable() {
    const [ host, port ] = this.connectionString.split(':');
    const timeout = this.connectionTimeout;

    logger.debug(`checkKafkaIsReachable ${host}:${port}`);

    return isPortReachable(port, { host, timeout })
  }

  createConsumerGroup({ groupId, topics, eventHandler }) {

    logger.debug('{ groupId, topics, eventHandler }', { groupId, topics, eventHandler });

    return new Promise((resolve, reject) => {

      const options = Object.assign(this.consumerGroupDefaultOptions, { groupId });

      logger.debug('options:', options);

      const consumerGroup = new ConsumerGroup(options, topics);

      // const groupOffset = new Offset(consumerGroup.client);

      consumerGroup.on('connect', () => {
        logger.debug(`Consumer Group '${groupId}' connected.`);
        resolve(consumerGroup);
      });

      consumerGroup.on('message', (message) => {

        logger.debug('on message:', util.inspect(message, false, 10));

        //TODO: use KafkaMessageProcessor

        const { error, event } = makeEvent(message.value);

        if (error) {
          throw new Error(error);
        }

        eventHandler(event)
          .then((event) => {

            logger.debug(`Message processed {${groupId}} {${event.entityType}} {${event.eventId}`);

            // const { topic, offset, partition } = event;
            //
            // const payload = { topic, offset, partition };
            //
            // logger.debug('payload:', payload);
            //
            // groupOffset.commit(groupId, [ payload ], (err, data) => {
            //
            //   if (err) {
            //     return Promise.reject(err);
            //   }
            //
            //   logger.debug('Commit data:', data);
            // });
          })
          .catch((err) => {
            logger.error(err);
          })

      });

      consumerGroup.on('error', (err) => {
        logger.error('ON error: ', err);
        // TODO: better handle { code: 'ECONNRESET' }
        reject(err);
      });

      consumerGroup.client.on('error', (data) => {
        logger.error('client ON error: ', data);
      });

      // consumerGroup.on('rebalancing', () => {
      //   logger.debug(`Consumer Group '${groupId}' rebalancing.`);
      // });
      //
      // consumerGroup.on('rebalanced', () => {
      //   logger.debug(`Consumer Group '${groupId}' rebalanced.`);
      // });
      //
      // consumerGroup.client.on('brokersChanged', (data) => {
      //   logger.debug('client ON brokersChanged: ', data);
      // });
      //
      // consumerGroup.client.on('reconnect', (data) => {
      //   logger.debug('client ON reconnect: ', data);
      // });
      //
      // consumerGroup.client.on('close', (data) => {
      //   logger.debug('client ON close: ', data);
      // });
      //
      // consumerGroup.client.on('ready', (data) => {
      //   logger.debug('client ON ready: ', data);
      // });
      //
      // consumerGroup.client.on('zkReconnect', (data) => {
      //   logger.debug('client ON zkReconnect: ', data);
      // });
    });

  }
}
