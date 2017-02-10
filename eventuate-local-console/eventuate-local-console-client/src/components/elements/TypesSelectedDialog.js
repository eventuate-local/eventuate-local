/**
 * Created by andrew on 12/11/16.
 */
import React, { Component } from 'react';
import { ListGroup, ListGroupItem } from 'react-bootstrap';
import * as BS from 'react-bootstrap';
import { GenericDialog } from './GenericDialog';
import { breakWords } from './utilities';
import { simpleHash } from '../../utils/hash';

export class TypesSelectedDialog extends Component {
  render() {
    const { items, dismiss, confirm, visible = false } = this.props;

    const footer = [
      (<div className="pull-left" key="l"><BS.Button onClick={ dismiss }>Cancel</BS.Button></div>),
      (<div className="pull-right" key="r"><BS.Button bsStyle="primary" onClick={ confirm }>Confirm</BS.Button></div>)];

    const dialogProps = {
      visible,
      dismiss,
      footer
    };

    return (<GenericDialog {...dialogProps} title="Confirm Subscription">
      <h5>You are getting subscribed to events of these Aggregate Types:</h5>
      <ListGroup>{
        items.map(i => (<ListGroupItem key={ simpleHash(i) }>{ breakWords(i) }</ListGroupItem>))
      }</ListGroup>
    </GenericDialog>);
  }
}
