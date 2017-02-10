/**
 * Created by andrew on 11/11/16.
 */
import React, { Component } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { Grid, Col } from "react-bootstrap";
import * as BS from "react-bootstrap";
import { LinkContainer } from "react-router-bootstrap";

import { LoadingIcon, SortToggler, CancelButton, SearchField, Timeago, breakWords } from '../elements';
import * as H from '../elements';
import * as A from '../../actions';
import { simpleHash } from '../../utils/hash';
import * as C from '../../constants';
import * as INST from '../../selectors/instances';

export class InstancesPage extends Component {
  render() {
    const { typeName } = this.props.params;
    const { refresh, sortItems, filterItems, resetFilter, datasource, items, sorting, filtering } = this.props;
    const { isLoading, lastUpdate, lastError } = datasource;

    const loadingIconProps = {
      isHidden: false,
      isLoading,
      refresh
    };

    const rows = items.length ? (items.map(({ aggregateId, type, created, updated, events = 0 }) => {
      const keyHash = simpleHash(aggregateId);
      return (<tr key={ keyHash }>
        <td><LinkContainer to={{ pathname: `/instances/${ typeName }/${ aggregateId }`}}>
          <BS.Button bsStyle="link">{ aggregateId }</BS.Button>
        </LinkContainer></td>
        <td><H.Timeago timestamp={ created } expanded /></td>
        <td><H.Timeago timestamp={ updated } expanded /></td>
        <td><span>{ events }</span></td>
      </tr>)
    })) : (<tr>
      <td colSpan="4">..</td>
    </tr>);

    const sortedHeaderProps = {
      sortState: sorting,
      toggleSort: sortItems,
      className: ' '
    };

    const filter = (<div className="shorten-form-group"><Col xs={ 12 } sm={ 8 } md={ 6 }>
      <H.SearchField state={{ value: filtering, placeholder: 'Filter by id' }} onChange={ filterItems } reset={ resetFilter } />
    </Col><BS.Clearfix /></div>);

    const updatedWhen = ((lastUpdate !== 0) && <span className="updated-when">Updated: <strong><H.Timeago timestamp={ lastUpdate } /></strong></span>);

    const foundEntries = (!!(items.length) && <span className="text-muted">({ items.length } entries)</span>);

    const errorMessage = (lastError && <BS.Alert bsStyle="danger">
      <h4>Oh snap! You got an error!</h4>
      <p>{ lastError }</p>
    </BS.Alert>);

    return <Grid className="page-instances">
      <Col xs={12} >
        <h2>Aggregate Type: <hr className="hidden-lg" /><strong>{ H.breakWords(typeName) }</strong><div className="pull-right">{ updatedWhen }<H.LoadingIcon {...loadingIconProps} /> <H.CancelButton to="/types" /></div></h2>
      </Col>
      <Col xs={12} >
        { errorMessage }
        <BS.Panel header={ filter }>
          <BS.Table striped bordered hover responsive fill>
            <thead>
            <tr>
              <th><div className="instances-header-cell"><H.SortToggler {...sortedHeaderProps} inline>Instance ID</H.SortToggler> { foundEntries }</div></th>
              <th><div className="instances-header-cell">Created</div></th>
              <th><div className="instances-header-cell">Updated</div></th>
              <th><div className="instances-header-cell">Events</div></th>
            </tr>
            </thead>
            <tbody>{ rows }</tbody>
          </BS.Table>
        </BS.Panel>
      </Col>
    </Grid>;
  }
}

export default connect((state, ownProps) => ({
  datasource: INST.getInstancesDataSource(ownProps.params.typeName)(state),
  items: INST.getSortedFilteredItems(ownProps.params.typeName)(state),
  allItems: INST.getInstancesItems(ownProps.params.typeName)(state),
  page: state.page,
  sorting: INST.getSortingState(state),
  filtering: INST.getFilteringState(state)
}), dispatch =>  bindActionCreators({
  refresh: A.fetchInstancesAsync,
  sortItems: A.cycleSorting.bind(null, C.PAGE_INSTANCES_KEY,  C.INSTANCE_ID_KEY),
  resetFilter: A.filterItemsReset.bind(null, C.PAGE_INSTANCES_KEY, C.INSTANCE_ID_KEY),
  filterItems: A.filterItemsAsync.bind(null, C.PAGE_INSTANCES_KEY, C.INSTANCE_ID_KEY)
}, dispatch), (stateProps, dispatchProps, ownProps) => ({
  ...ownProps,
  ...stateProps,
  refresh: () => dispatchProps.refresh(ownProps.params.typeName, true),
  sortItems: dispatchProps.sortItems,
  resetFilter: dispatchProps.resetFilter,
  filterItems: (evt) => dispatchProps.filterItems(evt, stateProps.allItems )
}))(InstancesPage);