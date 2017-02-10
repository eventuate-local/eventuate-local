/**
 * Created by andrew on 12/15/16.
 */
import { handleActions } from 'redux-actions';
import Immutable, { List } from 'immutable';
import invariant from 'invariant';
import { wsIncomingMessage } from '../actions/network';

export default handleActions({
  [wsIncomingMessage]: (state, action) => {
    const { message } = action.payload;
    const { timestamp = (new Date() - 0) } = action.meta || {};
    return state.push({
      timestamp,
      message
    });
  }
}, List())