/**
 * Created by andrew on 12/15/16.
 */
import React, { Component } from 'react';

export class ScrollablePane extends Component {

  componentWillUpdate () {
    const node = this.element;
    this.shouldScrollBottom = Math.abs(node.scrollTop + node.offsetHeight - node.scrollHeight) < 20;
  }

  componentDidUpdate() {
    if (this.shouldScrollBottom) {
      const node = this.element;
      node.scrollTop = node.scrollHeight
    }
  }

  render() {
    // const { component, ...rest } = this.props;
    return <div ref={(element) => { this.element = element; }}  {...this.props} />;
  }
}