/**
 * Created by andrew on 12/11/16.
 */
import { createAction, createActions } from 'redux-actions';
import { timestamp } from '../utils/timestamp';

export const { dataFetching, dataFetched, dataFetchFail, dataFetchReset } = createActions({
  DATA_FETCHING: (entity, key) => ({ entity, key }),
  DATA_FETCHED: [(entity, key, data) => ({ entity, key, data }), timestamp],
  DATA_FETCH_FAIL: (entity, key, err) => ({ entity, key, err }),
  DATA_FETCH_RESET: [(entity, key) => ({ entity, key }), timestamp],
});

