/**
 * Created by andrew on 11/21/16.
 */
import { createSelector } from 'reselect';
import * as C from '../constants';
import { sortAscIterator, sortDescIterator } from '../utils/sorting';
import { readStateJs, readStateJsForce } from '../utils/immutability';

const delim = `\0${123}\0`;
export const propSelector = (page, field) => {
  switch (page) {
    case C.PAGE_TYPES_KEY:
    case C.PAGE_EVENT_LOG_KEY: {
      return ({ name }) => name;
    }
    case C.PAGE_INSTANCES_KEY: {
      return ({ aggregateId }) => aggregateId;
    }
    case C.PAGE_INSTANCE_EVENTS_KEY: {
      switch (field) {
        case C.EVENT_TYPE_KEY: {
          return ({ eventType }) => eventType.split('.').reverse()[0];
        }
        case C.EVENT_TIMESTAMP_KEY: {
          return ({ eventId }) => parseInt(eventId.split('-')[0], 16);
        }
        case C.WILDCARD_KEY:
          return ({ eventId, eventType }) => [ eventId, eventType ].join(delim);

        default:
          return ({ eventId }) => eventId;
      }
    }
    default: {
      return K => K;
    }
  }
};

export const getDataSource = (state, page, keys) => readStateJsForce(state, ['data', page].concat(keys), {});
export const getDataItems = (state, page, keys) => readStateJsForce(state, ['data', page].concat(keys || [],  ['items']), []);
export const getDataMap = (state, page, keys) => readStateJsForce(state, ['data', page].concat(keys || [],  ['items']), {});
export const getGeneralDataList = (state, keys) => readStateJsForce(state, ['data'].concat(keys), []);
export const getGeneralDataItem = (state, keys) => readStateJsForce(state, ['data'].concat(keys), {});
export const getGeneralPrimitiveItem = (state, keys) => readStateJsForce(state, ['data'].concat(keys), null);
export const getBoolItem = (state, keys) => readStateJsForce(state, keys, false);
export const getPageState = (state) => state.page;

export const getCommonSortState = (state, path) => {
  const { sorting } = getPageState(state);
  return readStateJs(sorting, path, 0);
};

export const getCommonSortKeyState = (state, path) => {
  const { sorting } = getPageState(state);
  return readStateJs(sorting, path, null);
};

export const getCommonFilterState = (state, path) => {
  const { filtering } = getPageState(state);
  return readStateJs(filtering, path, '');
};

export const applySorting = (selector = (K) => K) =>
  (items, sortingState) => {
    switch (sortingState) {
      case -1: // desc
      case 1: // asc
        return [...items].sort((sortingState === -1) ? sortDescIterator(selector) : sortAscIterator(selector));
      default:
        return items;
    }
  };

export const applyFilter = (selector = (K) => K) =>
  (items, filter) => {
    filter = filter.toLowerCase();

    return ((filter) ?
      items.filter((i) => (selector(i).toLowerCase().indexOf(filter) >= 0)) :
      items);
  };

export const genericFilter = (selector) =>
  createSelector(
    [ (s, p) => s, (s, p) => p ],
    applyFilter(selector)
  );