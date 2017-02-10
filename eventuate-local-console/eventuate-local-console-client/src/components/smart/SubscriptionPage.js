/**
 * Created by andrew on 12/12/16.
 */
import React, { Component } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { Grid, Col, Navbar, NavItem, Nav, NavbarBrand } from "react-bootstrap";
import * as BS from "react-bootstrap";
import { IndexLinkContainer, LinkContainer } from "react-router-bootstrap";
import Immutable from 'immutable';
import * as SUB from '../../selectors/subscription';
import * as C from '../../constants';
import { LoadingIcon, SortToggler, SearchField, Timeago, TypesSelectedDialog, ScrollablePane, breakWords, Jsontree, showType } from '../elements';
import { EventsTable } from '../elements/EventsTable';

export class SubscriptionPage extends Component {
  constructor(...args) {
    super(...args);
    this.state = {
      open: true,
      // events: {}
    };
    this.handleProps(this.props, {});

    this.toggleCollapse = this.toggleCollapse.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    this.handleProps(nextProps, this.props);
  }

  handleProps(props, oldProps) {}

  toggleCollapse() {
    this.setState({
      open: !this.state.open
    });
  }

  render() {
    const { subbedTo, events } = this.props;
    const typesSet = new Set(subbedTo);
    const shownEvents = events.map(i => Immutable.fromJS(i).toJS()).filter(({ message: { entityType } }) => typesSet.has(entityType));

    const styles = {
      h4: {
        marginTop: '0'
      }
    };

    const isOpen = this.state.open;

    return (<div className="page-subscription">
        <div className="head">
          <Grid>
            <Col xs={ 12 }>
              <h3>Event Log: <BS.Button bsSize="sm" bsStyle="link" onClick={ this.toggleCollapse }><BS.Glyphicon glyph={ isOpen ? 'menu-up' : 'menu-down' } /></BS.Button>
                <div className="pull-right"><IndexLinkContainer to={{ pathname: '/events', query: { subKey: null} }}>
                  <BS.Button bsStyle="primary">Edit..</BS.Button>
                </IndexLinkContainer>
                </div>
              </h3>
              <BS.Collapse in={this.state.open}>
                <div>
                  <h4 style={ styles.h4 }>Types: </h4>
                  <ul>{ subbedTo.map(n => (<li key={ n }>{ breakWords(n) }</li>)) }</ul>
                </div>
              </BS.Collapse>
            </Col>
          </Grid>
        </div>
        <div className="terminal">
          <div className="terminal-wrapper">
            <ScrollablePane className="terminal-content">
              <EventsTable events={ shownEvents } />
            </ScrollablePane>
          </div>
        </div>
      </div>
      );
  }

}

function op_debug(arg) {
  console.log(arg);
  debugger;
  return arg;
}

export default connect((state, ownProps) => ({
  subbedTo: SUB.getSubbedItems(state, ownProps.params.subKey),
  events: SUB.getEvents(state)
}))(SubscriptionPage);