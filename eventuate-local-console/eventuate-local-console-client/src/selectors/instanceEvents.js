/**
 * Created by andrew on 11/24/16.
 */
import { createSelector } from 'reselect';
import { Map } from 'immutable';
import * as C from '../constants';
import { propSelector, applyFilter, applySorting, getDataItems, getDataSource, getCommonSortState, getCommonSortKeyState, getCommonFilterState, getPageState } from './common';

const selector = propSelector.bind(null, C.PAGE_INSTANCE_EVENTS_KEY);

export const getEventsItems = (typeName, instanceId) =>
  (state) => getDataItems(state, C.PAGE_INSTANCE_EVENTS_KEY, [typeName, instanceId]);

export const getEventsDataSource = (typeName, instanceId) =>
  (state) => getDataSource(state, C.PAGE_INSTANCE_EVENTS_KEY, [typeName, instanceId]);

export const getSortingKey = (state) => getCommonSortState(state, [ C.PAGE_INSTANCE_EVENTS_KEY, 'current' ]);
export const getSortingState = (state) => getCommonSortKeyState(state, [ C.PAGE_INSTANCE_EVENTS_KEY, getSortingKey(state) ]);
export const getFilteringState = (state) => getCommonFilterState(state, [ C.PAGE_INSTANCE_EVENTS_KEY, C.WILDCARD_KEY ]);

export const getExpansionState = (state) => {
  const page = getPageState(state);
  const toggle = page.toggle;
  const result = toggle.getIn([C.PAGE_INSTANCE_EVENTS_KEY], Map());
  return result.toJS();
};

const getFilteredItems = (typeName, instanceId) => createSelector(
  [ getEventsItems(typeName, instanceId), getFilteringState ],
  applyFilter(selector(C.WILDCARD_KEY))
);

export const getSortedFilteredItems = (typeName, instanceId) => createSelector(
  [ getFilteredItems(typeName, instanceId), getSortingState ],
  (state, props, key) => applySorting(selector(key))(state, props)
);
