/**
 * Created by andrew on 11/11/16.
 */
import React, { Component } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { Grid, Col, Navbar, NavItem, Nav, NavbarBrand } from "react-bootstrap";
import * as BS from "react-bootstrap";
import { LinkContainer } from "react-router-bootstrap";
import CSSTransitionGroup from 'react-transition-group/CSSTransitionGroup';

import { LoadingIcon, SortToggler, SearchField, Timeago, TypesSelectedDialog } from '../elements';
import * as H from '../elements';
import * as A from '../../actions';
import { simpleHash } from '../../utils/hash';
import * as AGG from '../../selectors/aggTypes';
import * as C from '../../constants';

class AnimatedTable extends Component {
  render() {
    return (<BS.Table striped bordered hover responsive fill className="selectable-rows" bsClass="selected-table table" { ...this.props } />);
  }
}

export class TypesPage extends Component {
  constructor(...args) {
    super(...args);
    this.handleProps(this.props);

    this.renderRow = this.renderRow.bind(this);
    this.invokeConfirmation = this.invokeConfirmation.bind(this);
    this.hideConfirmation = this.hideConfirmation.bind(this);
    this.confirmationSuccess = this.confirmationSuccess.bind(this);
  }
  componentWillReceiveProps(nextProps) {
    this.handleProps(nextProps);
  }
  handleProps(props) {}

  invokeConfirmation() {
    this.props.invokeDialog('TYPES');
  }
  hideConfirmation() {
    this.props.dismissDialog('TYPES');
  }
  confirmationSuccess() {
    this.hideConfirmation();
    this.props.initiateSubscription(this.props.selectedItems);
  }

  renderSelected() {
    const { refresh, sortTypeNames, filterTypeNames, resetFilter, datasource,
      items, selectedItems, sorting, filtering,
      selectable,
      dialogVisible,
      // selectType,
      deselectType,
      selectAllTypes,
      deselectAllTypes } = this.props;

    const selectedItemsMarkup = selectedItems.length ? [
      (<thead key="0"><tr>
        <th><div className="in-table-checkbox-wrap"><BS.Checkbox inline checked={ true } onChange={ deselectAllTypes } /></div></th>
        <th><div className="pull-left types-header-cell"><span className="">Type Name </span> <span className="text-muted">(Selected { selectedItems.length } entries)</span></div><div className="clearfix"></div></th>
      </tr></thead>),
      <CSSTransitionGroup key="1"
                          component="tbody"
                          transitionName="standard-animation-head"
                          transitionEnterTimeout={ 500 }
                          transitionLeaveTimeout={ 300 }>
        {
          (selectedItems.map(name => {
            const keyHash = simpleHash(name) + 's';
            return (<tr key={ keyHash }>
              <td><div className="in-table-checkbox-wrap"><BS.Checkbox inline checked={ true } onChange={ () => deselectType(name) } /></div></td>
              <td><LinkContainer to={{ pathname: `/instances/${ name }`}}>
                <BS.Button bsStyle="link">{ name }</BS.Button>
              </LinkContainer></td>
            </tr>)
          }))
        }
      </CSSTransitionGroup>
    ] : [];

    return {
      // dialog: (!!selectable && (<TypesSelectedDialog items={ selectedItems } dismiss={ this.hideConfirmation } confirm={ this.confirmationSuccess } visible={ dialogVisible }/>)),
      header: (!!selectable && (<th><div className="in-table-checkbox-wrap"><BS.Checkbox inline checked={ false } onChange={ () => selectAllTypes(items.map(({ name }) => name)) } /></div></th>)),
      button: (!!selectable && (<div className="pull-right"><BS.Button bsStyle="primary" bsSize="sm" onClick={ this.confirmationSuccess } disabled={ !selectedItems.length }>View Events..</BS.Button></div>)),
      table: (
    !!selectable && (<CSSTransitionGroup component={ AnimatedTable }
                                         transitionName="standard-animation"
                                         transitionEnterTimeout={ 500 }
                                         transitionLeaveTimeout={ 300 }
                                         fill
                                         bsClass={ selectedItems.length ? 'is-visible selected-table table': 'selected-table table' }
    >
      { selectedItemsMarkup }
    </CSSTransitionGroup>)
  )
    }
  }

  renderRow({ name }) {
    const {
      selectable,
      selectType,
    } = this.props;

    const keyHash = simpleHash(name);
    return (<tr key={ keyHash }>
      {
        !!selectable && (<td><div className="in-table-checkbox-wrap"><BS.Checkbox inline checked={ false } onChange={ () => selectType(name) } /></div></td>)
      }
      <td><LinkContainer to={{ pathname: `/instances/${ name }`}}>
        <BS.Button bsStyle="link">{ name }</BS.Button>
      </LinkContainer></td>
    </tr>)
  }

  render() {

    const {
      refresh, sortTypeNames, filterTypeNames, resetFilter, datasource, items, sorting, filtering,
      selectable
    } = this.props;

    const { isLoading, lastUpdate, lastError } = datasource;

    const loadingIconProps = {
      isHidden: false,
      isLoading,
      refresh
    };

    const sortedHeaderProps = {
      sortState: sorting,
      toggleSort: sortTypeNames
    };

    const selectableRender = this.renderSelected();

    const title = selectable ? "Events Log" : "Aggregate Types";

    const filter = (<div className="shorten-form-group"><Col xs={ 12 } sm={ 6 } md={ 8 }>
      <H.SearchField state={{ value: filtering, placeholder: 'Filter by name' }} onChange={ filterTypeNames } reset={ resetFilter } />
    </Col>
      <Col xs={ 12 } sm={ 6 } md={ 4 }>{ selectableRender.button }</Col><BS.Clearfix /></div>);

    return <Grid className="page-agg-types">
      <Col xs={ 12 }>
        <h2>{ title } <div className="pull-right">{ H.updatedWhen(lastUpdate) }<H.LoadingIcon {...loadingIconProps} /></div></h2>
        { !!selectable && (<h4>Select types to subscribe for:</h4>)}
      </Col>
      <Col xs={ 12 }>
        { H.errorMessage(lastError) }
        { selectableRender.dialog }
        <BS.Panel header={ filter }>
          { selectableRender.table }
          <BS.Table striped bordered hover responsive fill className="selectable-rows" bsClass="sorted-table table">
            <thead>
            <tr>
              { selectableRender.header }
              <th>
                <H.SortToggler {...sortedHeaderProps} inline>Type Name </H.SortToggler> {
                  H.foundEntriesHint(items)
                }</th>
            </tr>
            </thead>
            <CSSTransitionGroup component="tbody"
                                transitionName="standard-animation-foot"
                                transitionEnterTimeout={ 500 }
                                transitionLeaveTimeout={ 300 }>
              {
                items.length ?
                  (items.map(this.renderRow)) :
                  (<tr><td colSpan={ selectable ? 2 : 1 }>&mdash;</td></tr>)
              }
            </CSSTransitionGroup>
          </BS.Table>
        </BS.Panel>
      </Col>

    </Grid>;
  }
}

function op_debug(arg) {
  console.log(arg);
  debugger;
  return arg;
}

export default connect((state, ownProps) => ({
  datasource: AGG.getTypeDataSource(state),
  items: ownProps.selectable ? AGG.getSiftedItems(state, C.PAGE_EVENT_LOG_KEY) :
    AGG.getSortedFilteredItems(state, C.PAGE_TYPES_KEY), // AGG.getSortedFilteredItems(state),
  allItems: AGG.getTypeItems(state),
  selectedItems: AGG.getSelectedItems(state),
  sorting: AGG.getSortingState(state, ownProps.selectable ? C.PAGE_EVENT_LOG_KEY : C.PAGE_TYPES_KEY),
  filtering: AGG.getFilteringState(state, ownProps.selectable ? C.PAGE_EVENT_LOG_KEY : C.PAGE_TYPES_KEY),
  dialogVisible: AGG.getDialogVisibility(state)
}), (dispatch, ownProps) =>  bindActionCreators({
  refresh: A.fetchTypesAsync.bind(null, true),
  sortTypeNames: A.cycleSorting.bind(null, ownProps.selectable ? C.PAGE_EVENT_LOG_KEY : C.PAGE_TYPES_KEY,  C.AGG_TYPE_NAME_KEY),
  resetFilter: A.filterItemsReset.bind(null, ownProps.selectable ? C.PAGE_EVENT_LOG_KEY : C.PAGE_TYPES_KEY, C.AGG_TYPE_NAME_KEY),
  filterTypeNames: A.filterItemsAsync.bind(null, ownProps.selectable ? C.PAGE_EVENT_LOG_KEY : C.PAGE_TYPES_KEY, C.AGG_TYPE_NAME_KEY),
  selectType: A.aggTypesSelect,
  deselectType: A.aggTypesDeselect,
  selectAllTypes: A.aggTypesSelectAll,
  deselectAllTypes: A.aggTypesDeselectAll,
  invokeDialog: A.modalActivate,
  dismissDialog: A.modalDismiss,
  initiateSubscription: A.requestSubscriptionAsync
}, dispatch), (stateProps, dispatchProps, ownProps) => ({
  ...ownProps,
  ...stateProps,
  ...dispatchProps,
  filterTypeNames: (evt) => dispatchProps.filterTypeNames(evt, stateProps.allItems )
}))(TypesPage);