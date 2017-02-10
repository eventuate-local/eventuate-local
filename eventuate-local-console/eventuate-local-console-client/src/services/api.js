/**
 * Created by andrew on 11/18/16.
 */
import fetch from 'isomorphic-fetch';

const TYPES_FAIL = false;
const INSTANCES_FAIL = false;
const EVENTS_FAIL = false;

const fetchResponseHandler = (response) => {
  if (response.status >= 400) {
    throw new Error("Bad response from server");
  }
  return response.json()
    .then(({ error, ...rest }) => {
      if (error) {
        throw new Error(`Internal server error: ${ JSON.stringify(error)}`);
      }
      return rest;
    });
};

const GET_HEADERS = { cache: "no-cache" };
const JSON_HEADERS = {
  'Accept': 'application/json',
  'Content-Type': 'application/json'
};

export const api = {

  fetchTypes() {
    if (TYPES_FAIL /*Math.random() < .1*/) {
      return Promise.reject(new Error('Failed to load Types data.'))
    }
    return fetch('/api/types', { headers: GET_HEADERS }).then(fetchResponseHandler);
  },

  fetchInstances(typeName) {
    if (INSTANCES_FAIL /* Math.random() < .2 */) {
      return Promise.reject(new Error(`Failed to load Instances data for type '${ typeName}'.`))
    }
    return fetch(`/api/instances/${ typeName }`, { headers: GET_HEADERS }).then(fetchResponseHandler);
  },

  fetchEvents(typeName, instanceId) {
    if (EVENTS_FAIL /* Math.random() < .2 */) {
      return Promise.reject(new Error(`Failed to load Events data for type '${ typeName}' and id '${ instanceId }'.`))
    }
    return fetch(`/api/instance/${ typeName }/${instanceId}`, { headers: GET_HEADERS }).then(fetchResponseHandler);
  },

  requestSubscription(payload) {
    const body = JSON.stringify(payload);
    return fetch(`/api/subscription`, {
      method: 'POST',
      headers: JSON_HEADERS,
      body
    }).then(fetchResponseHandler);
  },

  checkSubscription(subKey) {
    if (!subKey || (subKey == 'undefined')) {
      return Promise.resolve({
        items: []
      });
    }
    return fetch(`/api/subscription/${ subKey }`, { headers: GET_HEADERS }).then(fetchResponseHandler);
  }
};