/**
 * Created by andrew on 12/15/16.
 */
import { handleActions } from 'redux-actions';
import Immutable, { Map } from 'immutable';
import invariant from 'invariant';
import { wsIncomingMessage } from '../actions/network';
import { eventsFetched } from '../actions/instanceEvents';
import type from 'istypes';
import { parseTimestamp } from '../utils/timestamp';

export default handleActions({
  [wsIncomingMessage]: (state, action) => {
    const { message = {} } = action.payload;
    const {
      entityId,
      entityType,
      eventData,
      eventId,
      eventType
    } = message;
    if (
      type.isUndefined(entityId) ||
      type.isUndefined(entityType) ||
      type.isUndefined(eventData) ||
      type.isUndefined(eventId) ||
      type.isUndefined(eventType)
    ) {
      return state;
    }

    const { timestamp = (new Date() - 0) } = action.meta || {};
    return state.set(eventId, {
      timestamp,
      message
    });
  },

  [eventsFetched]: (state, action) => {
    const { key, data } = action.payload;
    const [ entityType, entityId ] = key;
    const nextState = data.reduce((state, {
      eventData,
      eventId,
      eventType }) => state.set(eventId, ({
        timestamp: parseTimestamp(eventId),
        message: {
          entityId,
          entityType,
          eventData,
          eventId,
          eventType
        }
      })), state);

    return nextState;
  }
}, Map())
