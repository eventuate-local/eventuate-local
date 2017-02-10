/**
 * Created by andrew on 9/21/16.
 */
import React from 'react';
import { combineReducers, createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk';
import createLogger from 'redux-logger';
import { reduxReactRouter, routerStateReducer } from 'redux-router';
import { browserHistory } from 'react-router';
import reducers from './reducers';
import { getRoutes } from './components/smart/Routes';
import sequential from 'redux-actions-sequences';

const middleware = [ sequential ,/*  promise, */ thunk, createLogger()];

export default function getStore(initialState) {

  const appliedMiddleware =
    applyMiddleware(...middleware);

  const topReducer = combineReducers({
    router: routerStateReducer,
    ...reducers
  });

  let resolveDispatch = null;
  const storePromise = new Promise(rs => {
    resolveDispatch = rs;
  }).then(store => ({
    dispatch: store.dispatch,
    getState: store.getState
  }));

  const routes = getRoutes({ storePromise });
  const history = browserHistory;

  const store = compose(
    appliedMiddleware,
    reduxReactRouter({
      routes,
      history
    })
  )(createStore)(topReducer, initialState);

  resolveDispatch && resolveDispatch(store);
  resolveDispatch = null;

  if (module.hot) {
    module.hot.accept(() => {

      const nextRootReducer = combineReducers({
        router: routerStateReducer,
        ...(require('./reducers').default)
      });

      store.replaceReducer(nextRootReducer);
    });
  }

  return { store, routes };
}
