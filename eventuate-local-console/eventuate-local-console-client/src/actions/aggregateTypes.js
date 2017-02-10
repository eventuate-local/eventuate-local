/**
 * Created by andrew on 11/14/16.
 */
import { createAction, createActions } from 'redux-actions';
import { timestamp } from '../utils/timestamp';
import * as C from '../constants';

export const {
  aggTypesFetching, aggTypesFetched, aggTypesFetchedFail, aggTypesFetchReset,
  aggTypesSelect, aggTypesSelectAll, aggTypesDeselect, aggTypesDeselectAll
} = createActions({
  AGG_TYPES_FETCHING: () => ({ key: C.ALL_TYPES_KEY }),
  AGG_TYPES_FETCHED: [(data) => ({ data, key: C.ALL_TYPES_KEY }), timestamp],
  AGG_TYPES_FETCHED_FAIL: (err) => ({ err, key: C.ALL_TYPES_KEY }),
  AGG_TYPES_FETCH_RESET: [() => ({ key: C.ALL_TYPES_KEY }), timestamp],
  AGG_TYPES_SELECT: (name) => ({ name }),
  AGG_TYPES_SELECT_ALL: (names) => ({ names }),
  AGG_TYPES_DESELECT: (name) => ({ name }),
  AGG_TYPES_DESELECT_ALL: () => ({ }),
});
