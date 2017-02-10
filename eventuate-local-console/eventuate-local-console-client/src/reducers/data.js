/**
 * Created by andrew on 11/11/16.
 */
import { combineReducers } from 'redux-immutable';
import { dataListReducerCreator } from './helpers';
import * as C from '../constants';
import { aggTypesFetching, aggTypesFetched, aggTypesFetchedFail, aggTypesFetchReset } from '../actions/aggregateTypes';
import { instancesFetching, instancesFetched, instancesFetchedFail, instancesFetchReset } from '../actions/instances';
import { eventsFetching, eventsFetched, eventsFetchedFail, eventsFetchReset } from '../actions/instanceEvents';
import typesSelection from './typesSelection';
import subscription from './subscription';
import messages from './messages';
import events from './events';

export default combineReducers({
  [C.PAGE_TYPES_KEY]: dataListReducerCreator([
    aggTypesFetching, aggTypesFetched, aggTypesFetchedFail, aggTypesFetchReset
  ]),
  [C.PAGE_INSTANCES_KEY]: dataListReducerCreator([
    instancesFetching, instancesFetched, instancesFetchedFail, instancesFetchReset
  ]),
  [C.PAGE_INSTANCE_EVENTS_KEY]: dataListReducerCreator([
    eventsFetching, eventsFetched, eventsFetchedFail, eventsFetchReset
  ]),
  [C.PAGE_SUBSCRIPTION_KEY]: subscription,
  typesSelection,
  messages,
  events
});
