/**
 * Created by andrew on 11/24/16.
 */
import { createAction, createActions } from 'redux-actions';
import { timestamp } from '../utils/timestamp';

export const { eventsFetching, eventsFetched, eventsFetchedFail, eventsFetchReset } = createActions({
  EVENTS_FETCHING: (typeName, instanceId) => ({ key: [typeName, instanceId] }),
  EVENTS_FETCHED: [(typeName, instanceId, data) => ({ key: [typeName, instanceId], data }), timestamp],
  EVENTS_FETCHED_FAIL: (typeName, instanceId, err) => ({ key: [typeName, instanceId], err }),
  EVENTS_FETCH_RESET: [(typeName, instanceId) => ({ key: [typeName, instanceId] }), timestamp]
});