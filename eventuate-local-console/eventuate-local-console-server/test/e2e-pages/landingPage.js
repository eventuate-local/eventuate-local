/**
 * Created by andrew on 2/3/17.
 */

const landingCommands = {
  toEventsLog() {
    return this.waitForElementVisible('@eventsLogLink', 1000)
      .click('@eventsLogLink');
  }
};

export default {
  url: `http://${ process.env.DOCKER_HOST_IP || process.env.SERVICE_HOST || 'localhost' }:${ process.env.SERVICE_PORT || '3001' }`,
  commands: [ landingCommands ],
  elements: {
    heading: {
      selector: 'h2' // Aggregate Types
    },
    links: {
      selector: 'table.selectable-rows>tbody>tr>td>a'
    },
    linkOne: {
      selector: 'table.selectable-rows>tbody>tr:first-of-type>td>a' // net.chrisrichardson.eventstore.examples.todolist.backend.domain.TodoAggregate
    },
    linkTwo: {
      selector: 'table.selectable-rows>tbody>tr:nth-child(2)>td>a' // net.chrisrichardson.eventstore.examples.todolist.backend.domain.TodoBulkDeleteAggregate
    },
    headerCount: {
      selector: 'table.selectable-rows>thead>tr>th>span.text-muted' // (2 entries)
    },
    filterInput: {
      selector: '.panel-title span.input-group>input[type=text]' // Bul >> (1 entries)
    },
    eventsLogLink: {
      selector: '.nav.navbar-nav li:nth-child(2) a[href]'
    }
  }
};