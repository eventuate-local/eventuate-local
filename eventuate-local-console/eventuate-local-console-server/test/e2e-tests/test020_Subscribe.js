/**
 * Created by andrew on 2/2/17.
 */
import globals from '../e2e-globals/globals';

export default {
  '@tags': [],
  'User subscribes to all types': (client) => {
    const eventsPage = client.page.eventsLogPage();
    const landingPage = client.page.landingPage();

    landingPage
      .navigate()
      .toEventsLog();

    // eventsPage.navigate();

    client.pause(2000);

    client.saveScreenshot(`./reports/SCREENSHOT_${ globals.seed }_020_${ globals.nextFilenameIdx() }.png`);

    eventsPage.selectAll();

    client.saveScreenshot(`./reports/SCREENSHOT_${ globals.seed }_020_${ globals.nextFilenameIdx() }.png`);

    eventsPage.expect.element('@heading').to.be.visible;
    eventsPage.subscribe();

    client.saveScreenshot(`./reports/SCREENSHOT_${ globals.seed }_020_${ globals.nextFilenameIdx() }.png`);

    client.end();

  }
};