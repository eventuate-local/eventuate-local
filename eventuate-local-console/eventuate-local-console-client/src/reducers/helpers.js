/**
 * Created by andrew on 11/18/16.
 */
import { handleActions } from 'redux-actions';
import Immutable, { Map } from 'immutable';
import invariant from 'invariant';

const initialState = Immutable.fromJS({
  isLoading: false,
  isInitial: true,
  items: [],
  lastUpdate: 0,
  lastError: null
});

function extractPath({ payload = {}}) {
  const { key = []} = payload;
  return Array.isArray(key) ? key : [ key ];
}

export const dataListReducerCreator = ([fetchingAction, successAction, failedAction, resetAction, extraActions = {}]) => {

  invariant(fetchingAction, 'fetchingAction');
  invariant(successAction, 'successAction');
  invariant(failedAction, 'failedAction');
  invariant(resetAction, 'resetAction');

  return handleActions({
    [fetchingAction]: (state, action) => {
      const path = extractPath(action);
      const diff = Immutable.fromJS({
        isInitial: false,
        isLoading: true
      });
      const map = state.getIn(path, initialState).merge(diff);
      return state.setIn(path, map);
    },

    [successAction]: (state, action) => {
      const { data } = action.payload;
      const path = extractPath(action);
      const diff = Immutable.fromJS({
        isInitial: false,
        isLoading: false,
        items: data,
        lastError: null,
        lastUpdate: action.meta.timestamp
      });
      const map = state.getIn(path, initialState).merge(diff);
      return state.setIn(path, map);
    },

    [failedAction]: {
      next(state, action)  {
        const { err } = action.payload;
        const path = extractPath(action);
        const diff = Immutable.fromJS({
          isInitial: false,
          isLoading: false,
          lastError: err
        });
        const map = state.getIn(path, initialState).merge(diff);
        return state.setIn(path, map);
      },
      throw(state, action)  {
        const err = action.payload;
        const path = extractPath({ key: []});
        const diff = Immutable.fromJS({
          isInitial: false,
          isLoading: false,
          lastError: err.message || err.toString()
        });
        const map = state.getIn(path, initialState).merge(diff);
        return state.setIn(path, map);
      }
    },

    [resetAction]: (state, action) => {
      const path = extractPath(action);
      const diff = Immutable.fromJS({
        isInitial: false,
        isLoading: false,
        lastError: null,
        items: [],
        lastUpdate: action.meta.timestamp
      });
      const map = state.getIn(path, initialState).merge(diff);
      return state.setIn(path, map);
    },

    ...extraActions
  }, Map());
};
