/**
 * Created by andrew on 12/13/16.
 */
import { simpleHash } from '../utils/hash';
import EventuateLocalClient from 'eventuate-local-nodejs-client';
import { getLogger } from '../utils/logger';
import EventEmitter from 'events';

const logger = getLogger({ title: 'subscriptions' });
const eventuateClient = new EventuateLocalClient({ fromOffset: 'latest' });

const topicsMap = new Map();
const subscriptionsMap = new Map();

const currentSubscription = {
  topicsUnion: [],
  topicsUnionToken: recordSub([]),
  progress: Promise.resolve()
};

export function recordSub(topics) {
  const normalized = [...(new Set(topics))].sort();
  const token = String(simpleHash(normalized.join('\0')));
  topicsMap.set(token, normalized);
  logger.debug('topicsMap:', topicsMap);
  return token;
}

export const lookupSub = (token) => topicsMap.has(token) ? topicsMap.get(token) : [];

export const changeSubscription = ({ newTopics }, listeners) => {

  resubscribe(newTopics)
    .then(emitter => {
      if (!emitter) {
        return;
      }
      emitter.removeAllListeners();
      listeners.forEach(listener => emitter.on('event', listener));
    });

};

function resubscribe(nextUnion) {

  logger.debug('resubscribe()');
  logger.debug('nextUnion:', nextUnion);
  logger.debug('subscriptionsMap:', subscriptionsMap);
  return currentSubscription.progress = (currentSubscription.progress
    .catch(err => {
      logger.error('resubscribe() / progress: ', err);
    })
    .then(() => new Promise(resolve => {

      const oldToken = currentSubscription.topicsUnionToken;
      const nextToken = recordSub(nextUnion);

      if ((oldToken === nextToken) && subscriptionsMap.has(oldToken)) {
        logger.debug('(oldToken === nextToken) && subscriptionsMap.has(oldToken)', oldToken);
        logger.debug('subscriptionsMap.get(oldToken), emitter: ', Object.prototype.toString.call(subscriptionsMap.get(oldToken)));
        return resolve(subscriptionsMap.get(oldToken));
      }
      const unsubPromise = (oldToken && currentSubscription.topicsUnion.length) ? (() => {
          if (subscriptionsMap.has(oldToken)) {
            const emitter = subscriptionsMap.get(oldToken);
            emitter.removeAllListeners();
            subscriptionsMap.delete(oldToken);
          }

          return eventuateClient.unSubscribe({ subscriberId: oldToken })
            .catch((err) => {
              logger.error(`resubscribe() / unsubscribe: eventuateClient.unSubscribe(): ${oldToken}. Recovered.`, err);
            });
        })()
        : Promise.resolve();

      return unsubPromise.then(() => {

        const emitter = new EventEmitter();

        // emitter.setMaxListeners(0);

        const eventuateEventHandler = createEventuateEventHandler(emitter, nextToken);

        // subscriberId, topics, eventHandler
        logger.debug('eventuateClient subscribe params:', {
          subscriberId: nextToken,
          topics: nextUnion,
          eventHandler: eventuateEventHandler
        });

        return (nextUnion.length ? eventuateClient.subscribe(nextToken, nextUnion, eventuateEventHandler) : Promise.resolve())
          .catch(err => {
            logger.error('resubscribe() / subscribe: eventuateClient.subscribe() error: ', err);
          })
          .then(() => {

            currentSubscription.topicsUnionToken = nextToken;
            currentSubscription.topicsUnion = nextUnion;

            subscriptionsMap.set(nextToken, emitter);
            return resolve(emitter);

          });
      })

    })));
}


const createEventuateEventHandler = (emitter, id) =>
  event => {
    logger.debug('Emitter: ' + id + ', event:', event);
    emitter.emit('event', event);
    return Promise.resolve(event);
  };

