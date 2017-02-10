/**
 * Created by andrew on 11/9/16.
 */
import React, { Component } from 'react';
import { Provider, connect } from 'react-redux';
import { Route, IndexRoute, Redirect } from 'react-router';
import * as R from 'react-router';
import * as RR from 'redux-router';

import * as A from '../../actions';
import PageLayout from './PageLayout';
import TypesPage from './TypesPage';
import InstancesPage from './InstancesPage';
import EventsPage from './EventsPage';
import SubscriptionPage from './SubscriptionPage';
import { NotFound } from '../NotFound';
import * as S_SUB from '../../selectors/subscription';


const EVT = {
  ENTER: 'enter',
  LEAVE: 'leave'
};


const PATH = ((root, types, subs, instances, events, subscription) => ({
  root, types, subs, instances, events, subscription
}))('root','types', 'subs', 'instances', 'events', 'subscription');


function handle(storePromise, event, path) {
  return ({ location, params, ...rest }, replace, next) => {

    storePromise.then(({dispatch, getState}) => {
      switch (path) {

        case PATH.root: {
          switch (event) {
            case EVT.ENTER: {
              dispatch(A.establishWsConnectionAsync());
              dispatch(A.ensureWsInstanceSync);
              break;
            }
            case EVT.LEAVE: {
              dispatch(A.terminateWsConnectionAsync());
              break;
            }
          }
          break;
        }

        case PATH.types: {
          switch (event) {
            case EVT.ENTER: {
              dispatch(A.fetchTypesAsync());
              break;
            }
            case EVT.LEAVE: { break; }
          }
          break;
        }

        case PATH.subs: {
          switch (event) {
            case EVT.ENTER: {
              const currentKey = S_SUB.getCurrentKey(getState()); // get from store
              if (!params.subKey && currentKey) {
                return dispatch(RR.replace({
                  pathname: `/events/${ currentKey }`
                }))
              }
              dispatch(A.fetchTypesAsync());
              break;
            }
            case EVT.LEAVE: { break; }
          }
          break;
        }

        case PATH.instances: {
          switch (event) {
            case EVT.ENTER: {
              const { typeName } = params;
              dispatch(A.fetchInstancesAsync(typeName));
              break;
            }
            case EVT.LEAVE: { break; }
          }
          break;
        }

        case PATH.events: {
          switch (event) {
            case EVT.ENTER: {
              const { typeName, instanceId } = params;
              dispatch(A.fetchEventsAsync(typeName, instanceId));
              break;
            }
            case EVT.LEAVE: { break; }
          }
          break;
        }

        case PATH.subscription: {
          switch (event) {
            case EVT.ENTER: {
              const { subKey } = params;
              return dispatch(A.checkSubscriptionAsync(subKey)).then(next);
            }
            case EVT.LEAVE: { break; }
          }
          break;
        }

      }
    });

    if (event == EVT.ENTER) {
      return next();
    }
  };
}

export const getRoutes = ({ storePromise } = {}) => {

  const handleEnter = handle.bind(null, storePromise, EVT.ENTER);
  const handleLeave = handle.bind(null, storePromise, EVT.LEAVE);

  return (
    <Route path="/" component={ PageLayout }
           onEnter={ handleEnter(PATH.root) }
           onLeave={ handleLeave(PATH.root) } >

      <R.IndexRedirect to="types" />

      <Route path="types" component={ (props) => <TypesPage { ...props } /> }
             onEnter={ handleEnter(PATH.types) } />

      <Route path="instances/:typeName" component={ InstancesPage }
             onEnter={ handleEnter(PATH.instances) }  />

      <Route path="instances/:typeName/:instanceId" component={ EventsPage }
             onEnter={ handleEnter(PATH.events) }  />

      <Route path="events" component={ ({ children }) => children  }
             onEnter={ handleEnter(PATH.subs) } >

        <IndexRoute component={ (props) => <TypesPage { ...props } selectable /> } />

        <Route path=":subKey" component={ SubscriptionPage }
               onEnter={ handleEnter(PATH.subscription) }
               onLeave={ handleLeave(PATH.subscription) } />

      </Route>

      <Route path="*" component={ NotFound } />

    </Route>
  );
};

