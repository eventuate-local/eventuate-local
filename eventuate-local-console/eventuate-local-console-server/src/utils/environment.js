/**
 * Created by andrew on 12/2/16.
 */
import fs from 'fs';
import path from 'path';
import env from 'node-env-file';

(() => {
  const envFile = path.join(__dirname, '..', '..', '.env');

  if (fs.existsSync && fs.existsSync(envFile)) {
    env(envFile, {
      verbose: false,
      raise: false,
      logger: console,
      overwrite: true
    } );
  }
})();

export default process.env;