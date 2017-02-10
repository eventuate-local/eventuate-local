/**
 * Created by andrew on 11/16/16.
 */
import { createAction, createActions } from 'redux-actions';
import * as S_COMM from '../selectors/common';

export const cycleSorting = createAction('CYCLE_SORTING', (page, field) => ({ page, field }));
export const cycleSortingReset = createAction('CYCLE_SORTING_RESET', (page, field) => ({ page, field }));

export const filterItems = createAction('FILTER_DATA', (page, field, match) => ({ page, field, match }));
export const filterItemsReset = createAction('FILTER_DATA_RESET', (page, field) => ({ page, field }));


export const dataViewToggle = createAction('DATA_VIEW_TOGGLE', (page, id) => ({ path: [ page, id ]}));
export const dataViewToggleReset = createAction('DATA_VIEW_TOGGLE_RESET', (page) => ({ path: [ page ]}));


export const filterItemsAsync = (page, field, evt, allItems) =>
  (dispatch, getState) => {
    const path = [page, field];
    const nextFilter = evt.target.value;
    const state = getState();

    const selector = S_COMM.propSelector(page, field);
    const currentFilter = S_COMM.genericFilter(selector);

    const nextItems = currentFilter(allItems, nextFilter);

    if (nextItems.length) {
      return dispatch(filterItems(page, field, nextFilter));
    }

    const oldItems = currentFilter(allItems, S_COMM.getCommonFilterState(state, path));
    if (oldItems.length) {
      return dispatch(filterItems(page, field, nextFilter));
    }

    evt.preventDefault();
    evt.stopPropagation();
    return false;
  };