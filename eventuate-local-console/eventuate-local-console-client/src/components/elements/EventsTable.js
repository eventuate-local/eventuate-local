/**
 * Created by andrew on 1/19/17.
 */
import React, { Component } from 'react';
import { Grid, Col, Navbar, NavItem, Nav, NavbarBrand } from "react-bootstrap";
import * as BS from "react-bootstrap";
import { IndexLinkContainer, LinkContainer } from "react-router-bootstrap";
import { LoadingIcon, SortToggler, SearchField, Timeago, TypesSelectedDialog, ScrollablePane, breakWords, Jsontree, showType } from '../elements';
import { prepareMessage } from '../../utils/dataMessages';


export class EventsTable extends Component {
  constructor(...args) {
    super(...args);
    this.renderEvent = this.renderEvent.bind(this);
  }
  render() {
    const { events, hideEntity } = this.props;
    return (<BS.Table striped bordered hover responsive condensed fill bsClass="table" className="table-events-log">
      <thead>
      <tr>
        <th>Timestamp:</th>
        <th>Event (Type/Id):</th>
        { !hideEntity && <th>Aggregate (Type/Id):</th> }
        <th>Data:</th>
      </tr>
      </thead>
      <tbody>{
        events.length ?
          events.map(this.renderEvent) :
          (<tr><td colSpan={ hideEntity ? 3 : 4 }>&mdash;</td></tr>)
      }</tbody>
    </BS.Table>);
  }

  renderEvent({ timestamp, message }) {
    const { hideEntity } = this.props;
    const { id, parsed } = prepareMessage(message);

    const {
      entityId,
      entityType,
      eventId,
      eventType,
      eventData
    } = parsed;

    const styles = {
      code: {
        display: 'inline-block'
      }
    };

    return (<tr key={ id }>
      <td><Timeago timestamp={ timestamp } expanded /></td>
      <td>{ showType(eventType) }/<code style={ styles.code }>{ eventId }</code></td>
      { !hideEntity &&  <td><LinkContainer to={{ pathname: `/instances/${ entityType }`}}>
        <BS.Button bsStyle="link">{ showType(entityType) }</BS.Button>
      </LinkContainer>/<LinkContainer to={{ pathname: `/instances/${ entityType }/${ entityId }` }}>
        <BS.Button bsStyle="link"><code style={ styles.code }>{ entityId }</code></BS.Button>
      </LinkContainer></td>
      }
      <td><Jsontree data={ eventData } hideRoot={ false } /></td>
    </tr>);

  }
}