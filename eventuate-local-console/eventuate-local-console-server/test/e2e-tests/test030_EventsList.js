/**
 * Created by andrew on 2/2/17.
 */
import globals from '../e2e-globals/globals';

export default {
  '@tags': [],
  'User initiates an event ans sees it immediately in the log': (client) => {
    const eventsListPage = client.page.eventsListPage();
    // const eventsPage = client.page.eventsLogPage();
    // const landingPage = client.page.landingPage();

    eventsListPage.navigate();

    client.pause(2000);

    client.saveScreenshot(`./reports/SCREENSHOT_${ globals.seed }_030_${ globals.nextFilenameIdx() }.png`);

    eventsListPage.expect.element('@heading').to.be.visible;
    eventsListPage.wait();
    client.saveScreenshot(`./reports/SCREENSHOT_${ globals.seed }_030_${ globals.nextFilenameIdx() }.png`);


    eventsListPage.fireEvent();

    eventsListPage.waitForANewRecord();

    client.saveScreenshot(`./reports/SCREENSHOT_${ globals.seed }_030_${ globals.nextFilenameIdx() }.png`);

    client.end();

  }
};