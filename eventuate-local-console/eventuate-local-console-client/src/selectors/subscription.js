/**
 * Created by andrew on 12/13/16.
 */
import { createSelector } from 'reselect';
import * as C from '../constants';
import { Map, List } from 'immutable';
import { applySorting, applyFilter, getDataItems, getDataSource, getDataMap, getGeneralDataList, getGeneralPrimitiveItem, getCommonSortState, getCommonFilterState, propSelector, getBoolItem } from './common';

function op_debug(arg) {
  console.log(arg);
  debugger;
  return arg;
}

export const getSubbedItems = (state, key) => getGeneralDataList(state, [C.PAGE_SUBSCRIPTION_KEY, 'items', key]);
export const getCurrentKey = (state) => getGeneralPrimitiveItem(state, [C.PAGE_SUBSCRIPTION_KEY, 'current']);
export const getEvents = (state) => state['data'].getIn(['events'], List()).toArray().map(i => i.toJS ? i.toJS() : i).sort(({ timestamp: x}, { timestamp: y}) => x - y); //getGeneralPrimitiveItem(state, 'events') || Map()).toArray();