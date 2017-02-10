/**
 * Created by andrew on 12/14/16.
 */
import sockjs from 'sockjs';
import { isFSA } from 'flux-standard-action';
import * as S from './subscriptions';
import { getLogger } from '../utils/logger';

const logger = getLogger({ title: 'web-sockets' });

const ws = sockjs.createServer({
  sockjs_url: 'http://cdn.jsdelivr.net/sockjs/1.1.1/sockjs.min.js'
});

const clients = new Map(); // es6 Map
const topicsByClient = new Map();

ws.on('connection', conn => {

  // readyState
  // 0-connecting, 1-open, 2-closing, 3-closed.
  const {
    id: clientId,
    // readable, writable,
    remoteAddress, remotePort, address, url, pathname, prefix, protocol,
    headers, readyState,
    write, end
  } = conn;

  logger.info(`client connected: ${ clientId }`);


  logger.debug({
    // readable, writable,
    remoteAddress, remotePort, address, url, pathname, prefix, protocol, headers, readyState });

  conn.write = (function(send, ctx) {
    return function send_upg(message) {
      return send.call(ctx, JSON.stringify(message));
    };
  })(write, conn);

  // pathname: '/comm/851/9007198745958258/websocket'
  // const [ _blank1, _comm, _number, sessionId, subKey ] = pathname.split('/');

  logger.debug(`conn.id: ${conn.id}`);
  // logger.debug(`sessionId: ${sessionId}`);

  topicsByClient.set(clientId, []);
  clients.set(clientId, (data) => {
    logger.debug(`listener #${ clientId }, data:`, data);
    const topics = topicsByClient.get(clientId) || [];
    if (data.entityType && topics.indexOf(data.entityType) < 0) {
      logger.debug(`listener #${ clientId }: not subscribed`);
      return;
    }
    logger.debug(`listener #${ clientId }: sending..`);
    return conn.write(data);
  });

  conn.on('data', data => {
    logger.debug('on data:', data);

    const action = getJson(data);

    if (isFSA(action)) {
      const { data } = action.payload;
      logger.debug(`Received FSA of type: ${ action.type } payload:`, data);
      if (handleAction(conn, action)) {
        logger.debug(`Handled FSA of type: ${ action.type } payload:`, data);
      }
    } else {
      logger.error(new Error(`Incorrect message structure: ${data}`));
    }

  });

  conn.on('close', () => {
    logger.debug('client on close');
    if (clients.has(clientId)) {
      const topicsInfo = compareTopics(() => topicsByClient.delete(clientId));
      clients.delete(clientId);
      // TODO: add timeout maybe
      S.changeSubscription(topicsInfo, [...clients.values()]);
    }
  });

});

function handleAction(conn, { type, payload }) {
  switch (type) {
    case 'WS_SERVER_COMMAND': {
      const { verb, items } = payload.data;
      switch (verb) {
        case 'subscribe': {
          const { id: clientId } = conn;
          const topicsInfo = compareTopics(() => topicsByClient.set(clientId, items));
          logger.debug('topicsInfo:', topicsInfo);
          const { newTopics } = topicsInfo;
          const listeners = [...clients.values()];

          logger.debug('newTopics:', newTopics);
          logger.debug('listeners:', listeners);

          S.changeSubscription({ newTopics }, listeners);
          return true;
        }
      }
      break;
    }
  }
}

function compareTopics(changeFn) {
  logger.debug('compareTopics()');
  logger.debug('topicsByClient:', topicsByClient);
  const oldTopics = [...new Set(Array.prototype.concat.apply([], [...topicsByClient.values()]))].sort();
  logger.debug('oldTopics:', oldTopics);
  changeFn && changeFn();
  const newTopics = [...new Set(Array.prototype.concat.apply([], [...topicsByClient.values()]))].sort();
  logger.debug('newTopics:', newTopics);
  const areEqual = oldTopics.join('\0') === newTopics.join('\0');
  logger.debug('areEqual:', areEqual);

  return {
    oldTopics,
    newTopics,
    areEqual
  };
}

export const addSockJsHooks = (server) => {
  ws.installHandlers(server, {
    prefix: '/comm',
    log: (severity, message) => {
      logger.info(message);
    }
  });
  return server;
};

function getJson(input) {
  try {
    return JSON.parse(input);
  } catch (ex) {
    logger.error('JSON.parse error: ', ex);
    return null;
  }
}