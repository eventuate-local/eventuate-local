/**
 * Created by andrew on 2/3/17.
 */

import { initiateEvent } from '../e2e-globals/helpers';

const eventsLogListCommands = {
  wait() {
    this.api.pause(2000);
  },
  waitForANewRecord() {
    return this
      .click('@table')
      .waitForElementVisible('@tableRows', 30000);
  },
  fireEvent() {
    return this.api.pause(2000).perform(done => {
      initiateEvent().then(() => done(null), done);
    });
  }

};

export default {
  // http://192.168.99.100:3001/events/9007197632675634
  url: `http://${ process.env.DOCKER_HOST_IP || process.env.SERVICE_HOST || 'localhost' }:${ process.env.SERVICE_PORT || '3001' }/events/9007197632675634`,
  commands: [ eventsLogListCommands ],
  elements: {
    heading: {
      selector: 'h3' // Event Log:
    },
    table: {
      selector: 'table.table-events-log'
    },
    tableRows: {
      selector: 'table.table-events-log>tbody>tr>td:nth-child(2)'
    }
  }
};