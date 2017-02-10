/**
 * Created by andrew on 12/11/16.
 */
import { createAction, createActions } from 'redux-actions';
// import { timestamp } from '../utils/timestamp';

export const { modalActivate, modalDismiss } = createActions({
  MODAL_ACTIVATE: (name) => ({ name }),
  MODAL_DISMISS: (name) => ({ name })
});

