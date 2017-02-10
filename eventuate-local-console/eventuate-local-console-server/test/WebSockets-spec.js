import SockJS from 'sockjs-client';


const webSocketUrl = 'http://localhost:8080/comm';
const webSocketOptions = {
  transports: ['websocket'],
  'force new connection': true
};



const timeout = 15000;

describe('WebSockets', function () {

  this.timeout(timeout);

  it('should connect', (done) => {

    const sock = new SockJS(webSocketUrl, webSocketOptions);

    sock.onopen = function() {
      console.log('open');
      sock.send('{ "type": "test", "payload": { "data": "" } }');
      sock.close()

    };
    sock.onmessage = function(e) {
      console.log('message', e.data);
    };
    sock.onclose = function() {
      console.log('close');
      done();
    };

  });

});