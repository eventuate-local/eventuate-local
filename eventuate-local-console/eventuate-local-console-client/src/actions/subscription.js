/**
 * Created by andrew on 12/11/16.
 */
import { createAction, createActions } from 'redux-actions';
// import { timestamp } from '../utils/timestamp';

export const { subscribedTypes } = createActions({
  SUBSCRIBED_TYPES: ({ items, key}) => ({ items, key })
});
