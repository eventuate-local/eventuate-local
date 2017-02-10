/**
 * Created by andrew on 11/9/16.
 */
import React, { PropTypes } from "react";
import { connect } from 'react-redux';
import { Grid, Col, Navbar, NavItem, Nav } from "react-bootstrap";
import * as BS from "react-bootstrap";
import { Link } from "react-router";
import { IndexLinkContainer, LinkContainer } from "react-router-bootstrap";
import * as H from '../elements';

export class PageLayout extends React.Component {
  static propTypes = {
    children: PropTypes.node
  };

  render () {
    const { sockets } = this.props;
    return (
      <div className="react-root">
        <header>
          <Navbar className="main-nav">
            <LinkContainer to="/types">
              <Navbar.Brand>
                <Link to="/">Eventuate Console</Link>
              </Navbar.Brand>
            </LinkContainer>
            <Nav>
              <LinkContainer to="/types" onlyActiveOnIndex>
                <NavItem eventKey={ 1 }>Types</NavItem>
              </LinkContainer>
              <LinkContainer to="/events">
                <NavItem eventKey={ 2 }>Event Log</NavItem>
              </LinkContainer>
            </Nav>
            <BS.Navbar.Text pullRight>{
              H.indicator(sockets, 'WS:')
            }</BS.Navbar.Text>
          </Navbar>
        </header>
        <div className="app-body">
          <main className="app-content">
            {/*<Grid>*/}
              { this.props.children }
            {/*</Grid>*/}
          </main>
          <nav className="app-nav"></nav>
          <aside className="app-ads"></aside>
        </div>
        <footer>
          <Navbar className="footer-navigation">
            <Col xs={12} sm={6}>&copy; 2016 Eventuate.io</Col>
            <Col xs={12} sm={6} className="text-right">
              <a href="#">Terms</a> |&nbsp;
              <a href="#">Policy</a> |&nbsp;
              <a href="#">Contact</a> |&nbsp;
              <a href="#">About</a>
            </Col>
          </Navbar>

        </footer>

      </div>
    );
  }
}

export default connect((state) => ({
  sockets: state.page.sockets
}))(PageLayout);