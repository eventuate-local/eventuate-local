/**
 * Created by andrew on 12/13/16.
 */
import { handleActions } from 'redux-actions';
import Immutable, { Map, List } from 'immutable';
import invariant from 'invariant';
import { subscribedTypes } from '../actions/subscription';

export default handleActions({
  [subscribedTypes]: (state, action) => {
    const { items = [], key } = action.payload;
    if (!key) {
      return state.setIn(['current'], null);
    }
    return state.setIn(['current'], key).setIn(['items', key], items);
  }
}, Map());