/**
 * Created by andrew on 11/21/16.
 */
import { createSelector } from 'reselect';
import * as C from '../constants';
import { propSelector, applyFilter, applySorting, getDataItems, getDataSource, getCommonSortState, getCommonFilterState } from './common';

const selector = propSelector(C.PAGE_INSTANCES_KEY);

export const getInstancesItems = (typeName) => (state) => getDataItems(state, C.PAGE_INSTANCES_KEY, [typeName]);
export const getInstancesDataSource = (typeName) => (state) => getDataSource(state, C.PAGE_INSTANCES_KEY, [typeName]);

export const getSortingState = (state) => getCommonSortState(state, [ C.PAGE_INSTANCES_KEY, C.INSTANCE_ID_KEY ]);
export const getFilteringState = (state) => getCommonFilterState(state, [ C.PAGE_INSTANCES_KEY, C.INSTANCE_ID_KEY ]);

const getFilteredItems = (typeName) => createSelector(
  [ getInstancesItems(typeName), getFilteringState ],
  applyFilter(selector)
);

export const getSortedFilteredItems = (typeName) => createSelector(
  [ getFilteredItems(typeName), getSortingState ],
  applySorting(selector)
);



