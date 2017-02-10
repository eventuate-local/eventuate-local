/**
 * Created by andrew on 11/15/16.
 */
import { combineReducers } from 'redux';
import { handleAction, handleActions } from 'redux-actions';
import Immutable, { Map } from 'immutable';
import * as A from '../actions';
import * as C from '../constants';
import { updateStateJs } from '../utils/immutability';

const sorting = handleActions({
  [A.cycleSorting]: (state, action) => {
    const { page, field } = action.payload;
    const tempState = updateStateJs(state, [ page, 'current' ], () => field);
    return updateStateJs(tempState, [ page, field ], (x = 0) => ((x + 2) % 3 -1));
  },

  [A.cycleSortingReset]: (state, action) => {
    const { page, field } = action.payload;
    if (field == C.WILDCARD_KEY) {
      return updateStateJs(state, [ page ], () => ({
        'current': null
      }));
    }
    return updateStateJs(state, [ page, field ], () => 0);
  }
}, {});


const filtering = handleActions({
  [A.filterItems]: (state, action) => {
    const { page, field, match } = action.payload;
    return updateStateJs(state, [ page, field ], () => match || '');
  },

  [A.filterItemsReset]: (state, action) => {
    const { page, field } = action.payload;
    if (field == C.WILDCARD_KEY) {
      return updateStateJs(state, [ page ], () => {})
    }
    return updateStateJs(state, [ page, field ], () => '')
  }
}, {});

const toggle = handleActions({
  [A.dataViewToggle]: (state, action) => {
    const { path } = action.payload;
    const result = state.getIn(path, false);
    return state.setIn(path, !result);
  },
  [A.dataViewToggleReset]: (state, action) => {
    const { path } = action.payload;
    return state.setIn(path, Map());
  }
}, Map() /*Immutable.fromJS({})*/);

const modals = handleActions({
  [A.modalActivate]: (state, action) => {
    const { name } = action.payload;
    return {
      [name]: true
    };
  },
  [A.modalDismiss]: (state, action) => {
    return {};
  }
}, {});

// const network = handleActions({}, false);
const sockets = handleActions({
  [A.wsEstablished]: (state, action) => true,
  [A.wsLostConnection]: (state, action) => false,
  [A.wsTerminated]: (state, action) => false
}, false);

export default combineReducers({
  sorting,
  filtering,
  toggle,
  modals,
  // network,
  sockets
});
