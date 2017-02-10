/**
 * Created by andrew on 11/18/16.
 */
import { createAction, createActions } from 'redux-actions';
import { timestamp } from '../utils/timestamp';

export const { instancesFetching, instancesFetched, instancesFetchedFail, instancesFetchReset } = createActions({
  INSTANCES_FETCHING: (typeName) => ({ key: typeName }),
  INSTANCES_FETCHED: [(typeName, data) => ({ key: typeName, data }), timestamp],
  INSTANCES_FETCHED_FAIL: (typeName, err) => ({ key: typeName, err }),
  INSTANCES_FETCH_RESET: [(typeName) => ({ key: typeName }), timestamp]
});
