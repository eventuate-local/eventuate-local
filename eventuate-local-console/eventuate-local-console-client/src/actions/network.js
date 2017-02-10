/**
 * Created by andrew on 12/11/16.
 */
import { createAction, createActions } from 'redux-actions';
import { timestamp } from '../utils/timestamp';

export const {
  wsEstablished, wsTerminated, wsLostConnection, wsNotConnecting,
  wsIncomingMessage, wsEvent,
  wsServerCommand
} = createActions({
  WS_ESTABLISHED: () => ({}),
  WS_TERMINATED: () => ({}),
  WS_LOST_CONNECTION: () => ({}),
  WS_NOT_CONNECTING: [(code, reason) => ({ code, reason }), timestamp ],
  WS_INCOMING_MESSAGE: [(message) => ({message}), (_, timestamp) => timestamp ? ({timestamp}) : timestamp],
  WS_EVENT: (data) => ({ data }),
  WS_SERVER_COMMAND: (data) => ({ data })
});

export const wsLostConnectionAsync = () => dispatch => {
  const timestamp = new Date() - 0;
  const reconnectionAttempts = 3;
  dispatch(wsLostConnection());
  // TODO: Add reconnection logic
};