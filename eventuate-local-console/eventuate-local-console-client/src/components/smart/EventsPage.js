/**
 * Created by andrew on 11/11/16.
 */
import React, { Component } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { Grid, Col, } from "react-bootstrap";
import * as BS from "react-bootstrap";

import { LoadingIcon, getSortTogglerFor, CancelButton, SearchField, Timeago, Jsontree, breakWords } from '../elements';
import * as H from '../elements';
import { EventsTable } from '../elements/EventsTable';
import * as A from '../../actions';
import * as C from '../../constants';
import * as S_EVT from '../../selectors/instanceEvents';
import { parseTimestamp } from '../../utils/timestamp';

export class EventsPage extends Component {
  render() {

    const { typeName, instanceId } = this.props.params;

    const { refresh, sortingKey, sorting, sortBy, filtering, filterItems, resetFilter, datasource, items,
      viewExpansion, toggle
    } = this.props;
    const { isLoading, lastUpdate, lastError } = datasource;

    const loadingIconProps = {
      isHidden: false,
      isLoading,
      refresh
    };

    const sortingProps = { sortingKey, sorting, sortBy };
    const inlineSortingProps = { sortingKey, sorting, sortBy, inline: true, className: ' ' };

    const filter = (<div className="shorten-form-group"><Col xs={ 12 } sm={ 8 } md={ 6 }>
      <H.SearchField state={{ value: filtering, placeholder: 'Filter by event data' }} onChange={ filterItems } reset={ resetFilter } />
    </Col><BS.Clearfix /></div>);

    const updatedWhen = ((lastUpdate !== 0) && <span className="updated-when">Updated: <strong><H.Timeago timestamp={ lastUpdate } /></strong></span>);

    const foundEntries = (!!(items.length) && <span className="text-muted">({ items.length } entries)</span>);

    const errorMessage = (lastError && <BS.Alert bsStyle="danger">
      <h4>Oh snap! You got an error!</h4>
      <p>{ lastError }</p>
    </BS.Alert>);

    return <Grid className="page-events">
      <Col xs={12}>
        <h3>Aggregate Type: <hr className="hidden-lg" /><strong>{ H.breakWords(typeName) }</strong><div className="pull-right"><H.CancelButton to="/types" /></div></h3>
        <h2>Instance: <hr className="hidden-lg" /><strong>{ H.breakWords(instanceId) }</strong><div className="pull-right"><H.CancelButton to={ `/instances/${typeName}`} /></div></h2>
      </Col>
      <Col xs={12}>
        { errorMessage }
        <BS.Panel header={ filter }>
          <BS.Row>
            <Col xs={ 12 } sm={ 6 }><div className="events-header-cell">{ H.getSortTogglerFor(C.EVENT_TYPE_KEY, 'Events', inlineSortingProps) } { foundEntries }<div className="pull-right">{ updatedWhen }<H.LoadingIcon {...loadingIconProps} className="btn-xs" /></div></div></Col>
            <Col xs={ 12 } sm={ 6 }>{ H.getSortTogglerFor(C.EVENT_TIMESTAMP_KEY, 'Timestamp', sortingProps) }<div className="events-header-cell" style={{ display: 'none'}}/></Col>
          </BS.Row>
          <EventsTable events={ items.map(prepareEventData(typeName, instanceId)) } hideEntity />
        </BS.Panel>
      </Col>
      {/*<Col xs={12}></Col>*/}
    </Grid>;
  }
}

function prepareEventData(typeName, instanceId) {
  return ({ eventId, eventType, eventData }) => {
    return {
      timestamp: parseTimestamp(eventId),
      message: {
        entityId: instanceId,
        entityType: typeName,
        eventData,
        eventId,
        eventType
      }
    }
  }
}

export default connect((state, ownProps) => ({
  datasource: S_EVT.getEventsDataSource(ownProps.params.typeName, ownProps.params.instanceId)(state),
  items: S_EVT.getSortedFilteredItems(ownProps.params.typeName, ownProps.params.instanceId)(state),
  allItems: S_EVT.getEventsItems(ownProps.params.typeName, ownProps.params.instanceId)(state),
  page: state.page,
  sortingKey: S_EVT.getSortingKey(state),
  sorting: S_EVT.getSortingState(state),
  filtering: S_EVT.getFilteringState(state),
  viewExpansion: S_EVT.getExpansionState(state)
}), dispatch =>  bindActionCreators({
  refresh: A.fetchEventsAsync,
  sortBy: A.cycleSorting.bind(null, C.PAGE_INSTANCE_EVENTS_KEY),
  toggle: A.dataViewToggle.bind(null, C.PAGE_INSTANCE_EVENTS_KEY),
  filterItems: A.filterItemsAsync.bind(null, C.PAGE_INSTANCE_EVENTS_KEY, C.WILDCARD_KEY),
  resetFilter: A.filterItemsReset.bind(null, C.PAGE_INSTANCE_EVENTS_KEY, C.WILDCARD_KEY)
}, dispatch), (stateProps, dispatchProps, ownProps) => ({
  ...ownProps,
  ...stateProps,
  ...dispatchProps,
  refresh: () => dispatchProps.refresh(ownProps.params.typeName, ownProps.params.instanceId, true),
  filterItems: (evt) => dispatchProps.filterItems(evt, stateProps.allItems )
}))(EventsPage);