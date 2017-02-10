/**
 * Created by andrew on 11/9/16.
 */
import React, { Component } from 'react';
import { Provider } from 'react-redux';
import getStore from './configureStore';
import {
  ReduxRouter
} from 'redux-router';

export const getRoot = (initialState) => {

  const { store } = getStore(initialState);
  const root = (
    <Provider store={store}>
      <ReduxRouter />
    </Provider>
  );

  return Promise.resolve({
    root,
    store
  });

};