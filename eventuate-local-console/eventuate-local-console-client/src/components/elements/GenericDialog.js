/**
 * Created by andrew on 12/11/16.
 */
import React, { Component } from 'react';
import { Modal } from 'react-bootstrap';

export class GenericDialog extends Component {
  render() {
    const { visible, dismiss = null, dismissable = true, title, footer, headerProps, bodyProps, footerProps } = this.props;
    return (<Modal show={ visible } onHide={ dismiss }>
      <Modal.Header closeButton={ dismissable } {...headerProps}>
        { title && (<Modal.Title>{ title }</Modal.Title>)}
      </Modal.Header>
      <Modal.Body {...bodyProps}>
        { this.props.children }
      </Modal.Body>
      { footer && (<Modal.Footer {...footerProps}>{ footer }</Modal.Footer>)}
    </Modal>);
  }
}
