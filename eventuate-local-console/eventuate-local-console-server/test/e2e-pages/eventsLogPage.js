/**
 * Created by andrew on 2/3/17.
 */
const eventsLogCommands = {
  subscribe() {
    return this.waitForElementVisible('@button', 1000)
      .click('@button')
      .waitForElementNotPresent('@button');
  },
  selectAll() {
    return this.waitForElementVisible('@checkboxes', 1000)
      .click('@checkboxOne');
  }
};

export default {
  url: `http://${ process.env.DOCKER_HOST_IP || process.env.SERVICE_HOST || 'localhost' }:${ process.env.SERVICE_PORT || '3001' }/events`,
  commands: [ eventsLogCommands ],
  elements: {
    heading: {
      selector: 'h2' // Events Log
    },
    checkboxes: {
      selector: 'input[type=checkbox]'
    },
    checkboxOne: {
      selector: 'th input[type=checkbox]' //
    },
    headerCount: {
      selector: 'table.selectable-rows>thead>tr>th>div.types-header-cell>span.text-muted' // (Selected 2 entries)
    },
    button: {
      selector: '.panel-heading button[type=button].btn-primary' // View Events..
    }
  }
};