/**
 * Created by andrew on 11/17/16.
 */
import { createSelector } from 'reselect';
import * as C from '../constants';
import { applySorting, applyFilter, getDataItems, getDataSource, getGeneralDataList, getCommonSortState, getCommonFilterState, propSelector, getBoolItem } from './common';

const selector = propSelector(C.PAGE_TYPES_KEY);

export const getTypeItems = (state) => getDataItems(state, C.PAGE_TYPES_KEY, [C.ALL_TYPES_KEY]);
export const getTypeDataSource = (state) => getDataSource(state, C.PAGE_TYPES_KEY, [C.ALL_TYPES_KEY]);

function op_debug(arg) {
  console.log(arg);
  debugger;
  return arg;
}

export const getSortingState = (state, page) => getCommonSortState(state, [ page, C.AGG_TYPE_NAME_KEY ]);
export const getFilteringState = (state, page) => getCommonFilterState(state, [ page, C.AGG_TYPE_NAME_KEY ]);

export const getSelectedItems = (state) => getGeneralDataList(state, ['typesSelection']);

const getFilteredItems = createSelector(
  [ getTypeItems, getFilteringState ],
  applyFilter(selector)
);

export const getSortedFilteredItems = createSelector(
  [ getFilteredItems, getSortingState ],
  applySorting(selector)
);

export const getSiftedItems = createSelector(
  [ getSortedFilteredItems, getSelectedItems],
  (all, selected) => {
    const selectedSet = new Set(selected);
    return all.filter(({ name }) => !selectedSet.has(name));
  }
);

export const getDialogVisibility = (state) => getBoolItem(state, ['page', 'modals', 'TYPES']);

