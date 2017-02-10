/**
 * Created by andrew on 11/18/16.
 */
import * as RR from 'redux-router';
import { dispatchActionWhen } from 'redux-actions-sequences';

import { api } from '../services/api';
import * as C from '../constants';
import * as AGG from './aggregateTypes';
import * as INST from './instances';
import * as INST_EVT from './instanceEvents';
import * as SUB from './subscription';
import * as D from './dataTransform';
import * as N from './network';
import * as SOCK from './sockets';

import * as S_AGG from '../selectors/aggTypes';
import * as S_INST from '../selectors/instances';
import * as S_INST_EVT from '../selectors/instanceEvents';
import * as S_EVT from '../selectors/subscription';

const ensureInstanceSynced = ({ action }) =>
  (dispatch) => {
    const { message = {} } = action.payload;
    const {
      entityId,
      entityType
    } = message;

    if (!entityType) {
      return;
    }

    return checkTypeNameExists(dispatch, entityType, true)
      .catch(() => dispatch(fetchTypesAsync(true)))
      .then(() => {
        if (!entityId) {
          return;
        }
        checkInstanceIdExists(dispatch, entityType, entityId, true)
          .catch(() => dispatch(fetchInstancesAsync(entityType, true)));
      });

  };

export const ensureWsInstanceSync = dispatchActionWhen(ensureInstanceSynced, () => N.wsIncomingMessage);

const internalFetchTypesAsync = (dispatch, forced) => {
  dispatch(AGG.aggTypesFetching());
  return api.fetchTypes().then(
    ({ items }) => {
      dispatch(AGG.aggTypesFetched(items));
      if (!forced) {
        dispatch(D.filterItemsReset(C.PAGE_TYPES_KEY, C.AGG_TYPE_NAME_KEY));
        dispatch(D.cycleSortingReset(C.PAGE_TYPES_KEY, C.AGG_TYPE_NAME_KEY));
      }
      return items;
    },
    (err) => {
      dispatch(AGG.aggTypesFetchedFail(err.message));
      return [];
    }
  );
};

const internalFetchInstancesAsync = (dispatch, typeName, forced) => {
  dispatch(INST.instancesFetching(typeName));
  return api.fetchInstances(typeName).then(
    ({ items }) => {
      dispatch(INST.instancesFetched(typeName, items));
      if (!forced) {
        dispatch(D.filterItemsReset(C.PAGE_INSTANCES_KEY, C.WILDCARD_KEY));
        dispatch(D.cycleSortingReset(C.PAGE_INSTANCES_KEY, C.WILDCARD_KEY));
      }
      return items;
    },
    (err) => {
      dispatch(INST.instancesFetchedFail(typeName, err.message));
      return [];
    }
  )
};

const internalFetchEventsAsync = (dispatch, typeName, instanceId, forced) => {
  dispatch(INST_EVT.eventsFetching(typeName, instanceId));
  return api.fetchEvents(typeName, instanceId).then(
    ({ items }) => {
      dispatch(INST_EVT.eventsFetched(typeName, instanceId, items));
      if (!forced) {
        dispatch(D.filterItemsReset(C.PAGE_INSTANCE_EVENTS_KEY, C.WILDCARD_KEY));
        dispatch(D.cycleSortingReset(C.PAGE_INSTANCE_EVENTS_KEY, C.WILDCARD_KEY));
        dispatch(D.dataViewToggleReset(C.PAGE_INSTANCE_EVENTS_KEY));
      }
      return items;
    },
    (err) => {
      dispatch(INST_EVT.eventsFetchedFail(typeName, instanceId, err.message));
      return [];
    }
  )
};

const checkTypeNameExists = (dispatch, typeName, noRouting) => {
  return dispatch(fetchTypesAsync())
    .then((items) => {

      const [ typeItem ] = items.filter(({ name }) => (name == typeName));
      if (typeItem) {
        // typeItem exists, continue:
        return Promise.resolve();
      }

      if (!noRouting) {
        dispatch(RR.replace({
          pathname: `/404`, query: {
            page: 'types',
            typeName
          }
        }));
      }

      return Promise.reject();

    });
};

const checkInstanceIdExists = (dispatch, typeName, instanceId, noRouting) => {
  return dispatch(fetchInstancesAsync(typeName))
    .then((items) => {
      const [ instanceItem ] = items.filter(({ aggregateId }) => (aggregateId == instanceId));
      if (instanceItem) {
        // instanceItem exists, continue:
        return Promise.resolve();
      }

      if (!noRouting) {
        dispatch(RR.replace({
          pathname: `/404`, query: {
            page: 'instances',
            typeName,
            instanceId
          }
        }));
      }

      return Promise.reject();

    });
};

export const fetchTypesAsync = (forced) =>
  (dispatch, getState)  => {
    const items = S_AGG.getTypeItems(getState());
    return (items.length && !forced) ? Promise.resolve(items) : internalFetchTypesAsync(dispatch, forced);
  };

export const fetchInstancesAsync = (typeName, forced) =>
  (dispatch, getState) => {

    return checkTypeNameExists(dispatch, typeName)
      .then(() => {
        const items = S_INST.getInstancesItems(typeName)(getState());
        return (items.length && !forced) ? Promise.resolve(items) : internalFetchInstancesAsync(dispatch, typeName, forced);
      });
  };

export const fetchEventsAsync = (typeName, instanceId, forced) =>
  (dispatch, getState) => {
    return checkTypeNameExists(dispatch, typeName)
      .then(checkInstanceIdExists.bind(null, dispatch, typeName, instanceId))
      .then(() => {
        const items = S_INST_EVT.getEventsItems(typeName, instanceId)(getState());
        return (items.length && !forced) ? Promise.resolve(items) : internalFetchEventsAsync(dispatch, typeName, instanceId, forced);
      });
  };

export const requestSubscriptionAsync = (items) =>
  (dispatch, getState) => {

    return api.requestSubscription(items).then(
      ({ key, items, ...rest }) => {

        dispatch(SUB.subscribedTypes({
          key,
          items
        }));

        return dispatch(RR.push({
          pathname: `/events/${ key }`
        }));
      },
      (err) => {
        dispatch(N.wsLostConnection(err));
      });
  };

function ensureSubscription(subKey, state) {
  return api.checkSubscription(subKey)
    .then(({ key, items }) => {
      if (!items.length) {
        const items = S_EVT.getSubbedItems(state, subKey);
        if (!items || !items.length) {
          return Promise.reject('404');
        }
        return api.requestSubscription(items);
      }
      return Promise.resolve({ key, items });
    });
}

export const checkSubscriptionAsync = (subKey) =>
  (dispatch, getState) => {

    return ensureSubscription(subKey, getState())
      .catch((err) => {
        if (err == '404') {
          return dispatch(RR.replace({
            pathname: `/events`
          }));
        }
        return Promise.reject(err);
      })
      .then(({ key = null, items = [] } = {}) => {
        dispatch(SUB.subscribedTypes({
          key,
          items
        }));
        if (!key) {
          return Promise.resolve();
        }
        return dispatch(SOCK.wsRequestSubscriptionAsync({ key, items }));
      });
  };

