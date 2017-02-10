/**
 * Created by andrew on 11/14/16.
 */
import React, { Component } from 'react';
import { Grid } from 'react-bootstrap';

export class NotFound extends Component {
  render() {
    const { params, location } = this.props;
    const { query } = location;
    const { page, ...rest } = query;
    return (<Grid>
      <h1>404: Resource not found</h1>
      <pre>
        <div>Page: <strong>{ page }</strong></div>
        { Object.keys(rest).map(k => (<div key={ k }>{ k } : <strong>{ rest[k] }</strong></div>))}
      </pre>
    </Grid>);
  }
}
