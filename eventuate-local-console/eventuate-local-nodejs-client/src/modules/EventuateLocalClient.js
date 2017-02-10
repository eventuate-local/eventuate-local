import { getLogger } from './logger';
import KafkaAggregateSubscriptions from './KafkaAggregateSubscriptions';
import Result from './Result';

const logger = getLogger({ title: 'EventuateLocalClient' });

export default class EventuateLocalClient {

  constructor({ fromOffset } = {}) {
    this.subscriptions = {};
    this.fromOffset = fromOffset;
  }

  create(entityTypeName, _events, callback) {

    return new Promise((resolve, reject) => {

    });

  }

  loadEvents(entityTypeName, entityId, callback) {

    return new Promise((resolve, reject) => {

    });
  }

  update(entityTypeName, entityId, entityVersion, _events, callback) {

    return new Promise((resolve, reject) => {

    });
  }

  subscribe(subscriberId, topics, eventHandler, options, callback) {

    return new Promise((resolve, reject) => {

      if (!callback && typeof options == 'function') {
        callback = options;
      }

      const result = new Result({ resolve, reject, callback });

      if (!subscriberId || (typeof subscriberId != 'string')) {
        return result.failure(new Error(`Incorrect input parameter 'subscriberId' : ${subscriberId}`));
      }

      if (typeof topics == 'object') {

        if (!Array.isArray(topics)) {
          topics = Object.keys(topics);
        }

      } else {
        return result.failure(new Error('Incorrect input parameter \'topics\': should be an array'));
      }

      if (!topics.length) {
        return result.failure(new Error('Incorrect input parameter \'topics\': should not be empty'));
      }
      if ((typeof eventHandler !== 'function')) {
        return result.failure(new Error(`Incorrect input parameter \'eventHandler\': ${JSON.stringify(eventHandler)}`));
      }

      if (this.subscriptions[subscriberId]) {
        return result.failure(new Error(`The subscription ${subscriberId} already exists`));
      }

      if (!this.kafkaAggregate) {
        this.kafkaAggregate = new KafkaAggregateSubscriptions({ fromOffset: this.fromOffset });
      }

      logger.debug('EventuateLocalClient::kafkaAggregate', this.kafkaAggregate);

      this.kafkaAggregate.subscribe({ subscriberId, eventHandler, topics })
        .then((consumerGroup) => {

          // logger.debug('subscribe consumerGroup:', consumerGroup);
          this.addSubscription({ subscriberId, consumerGroup });
          result.success(consumerGroup);
        })
        .catch((err) => {
          logger.error(err);
          result.failure(err);
        });

    });
  }

  unSubscribe({ subscriberId }) {

    return new Promise((resolve, reject) => {
      logger.debug(`unSubscribe() ${subscriberId}`);

      const consumerGroup = this.subscriptions[subscriberId];

      if (!consumerGroup) {
        reject(new Error(`Subscription '${subscriberId}' not found.`));
      }

      consumerGroup.close((err) => {

        this.removeSubscription({ subscriberId });

        if (err) {
          return reject(err);
        }

        resolve();
      });


    });

  }

  removeSubscription({ subscriberId }) {
    delete this.subscriptions[subscriberId];
  }

  addSubscription({ subscriberId, consumerGroup }) {
    if (this.subscriptions[subscriberId]) {
      throw new Error(`Subscription '${subscriberId}' already exists.`);
    }

    this.subscriptions[subscriberId] = consumerGroup;
  }

  disconnect({ force = false }) {

    //TODO: write test

    Object.keys(this.subscriptions).map((subscriberId) => {
      this.subscriptions[subscriberId].kafkaAggregate.close({ force });
    });
  }

  serialiseObject(obj) {

    if (typeof obj == 'object') {
      return Object.keys(obj)
        .map(key => {
          return `${key}=${obj[key]}`;
        })
        .join('&');
    }
  }

  addBodyOptions (jsonData, options) {

    if (typeof options == 'object') {
      Object.keys(options).reduce((jsonData, key) => {

        jsonData[key] = options[key];

        return jsonData;
      }, jsonData);
    }
  }

  prepareEvents(events) {

    return events.map(({ eventData, ...rest } = event) => {

      if (typeof eventData == 'object') {
        eventData = JSON.stringify(eventData);
      }

      return {
        ...rest,
        eventData
      };
    });
  }

  eventDataToObject(events) {

    return events.map(e => {

      const event = Object.assign({}, e);

      if (typeof event.eventData != 'string') {
        return event;
      }

      try {
        event.eventData = JSON.parse(event.eventData);
      } catch (err) {
        logger.error('Can not parse eventData');
        logger.error(err);
        event.eventData = {};
      }

      return event;
    });
  }

  /**
   * Checks that events have all needed properties
   * Checks eventData
   * @param {Object[]} events - Events
   * @param {string} events[].eventType - The type of event
   * @param {string|Object} events[].eventData - The event data
   * @returns {Boolean}
   */
  checkEvents (events) {

    if (!Array.isArray(events) || !events.length) {
      return false;
    }

    return events.every(({ eventType, eventData }) => {

      if (!eventType || !eventData) {
        return false;
      }

      let ed;

      if (typeof eventData == 'string') {

        ed = eventData;
        //try to parse eventData
        try {
          ed = JSON.parse(ed);
        } catch(e) {
          return false;
        }
      } else if (typeof eventData == 'object') {
        ed = Object.assign({}, eventData);
      } else {
        return false;
      }

      if (Object.keys(ed).length === 0 ) {
        return false;
      }

      return true;

    });
  }
}
