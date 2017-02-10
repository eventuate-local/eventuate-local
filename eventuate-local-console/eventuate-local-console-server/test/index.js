/**
 * Created by andrew on 11/30/16.
 */
import http from 'http';
import assert from 'assert';

import '../src/index.js';

describe('Example Node Server', () => {
  it('should return 200', done => {
    http.get('http://127.0.0.1:8080', res => {
      assert.equal(200, res.statusCode);
      done();
    });
  });
});