/**
 * Created by andrew on 2/2/17.
 */
import globals from '../e2e-globals/globals';

export default {
  '@tags': [],
  'Landing page: Types': (client) => {
    const landingPage = client.page.landingPage();

    landingPage.navigate();

    client.pause(2000);

    client.assert.urlContains('/types');

    client.saveScreenshot(`./reports/SCREENSHOT_${ globals.seed }_010_${ globals.nextFilenameIdx() }.png`);

    landingPage.waitForElementVisible('@heading');
    // landingPage.expect.element('@formError').to.be.visible;

    client.saveScreenshot(`./reports/SCREENSHOT_${ globals.seed }_010_${ globals.nextFilenameIdx() }.png`);

    landingPage.expect.element('@heading').to.be.visible;
    landingPage.expect.element('@filterInput').to.be.visible;
    landingPage.expect.element('@links').to.be.visible;
    landingPage.expect.element('@linkOne').to.be.visible;
    landingPage.expect.element('@linkTwo').to.be.visible;
    landingPage.expect.element('@headerCount').text.to.equal('(2 entries)');


    client.saveScreenshot(`./reports/SCREENSHOT_${ globals.seed }_010_${ globals.nextFilenameIdx() }.png`);

    client.end();

  }
};