/**
 * Created by andrew on 12/9/16.
 */
import { handleActions } from 'redux-actions';
import Immutable, { List } from 'immutable';
import invariant from 'invariant';
import { aggTypesSelect, aggTypesSelectAll, aggTypesDeselect, aggTypesDeselectAll } from '../actions/aggregateTypes';

export default handleActions({
  [aggTypesSelect]: (state, action) => {
    const { name } = action.payload;
    const idx = state.indexOf(name);
    invariant(idx === -1, 'UI should not allow selection of the same type name');
    return [ ...state, name ];
  },
  [aggTypesSelectAll]: (state, action) => {
    const { names } = action.payload;
    return [ ...state, ...names ];
  },
  [aggTypesDeselect]: (state, action) => {
    const { name } = action.payload;
    const idx = state.indexOf(name);
    if (idx == -1) {
      return state;
    }
    const newState = [ ...state ];
    newState.splice(idx, 1);
    return newState;
  },
  [aggTypesDeselectAll]: (state, action) => {
    return [];
  }
}, []);