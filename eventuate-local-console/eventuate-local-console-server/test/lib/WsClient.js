'use strict';
import SockJS from 'sockjs-client';
import EventEmitter from 'events';

export default class WsClient extends EventEmitter {

  constructor({ webSocketUrl }) {
    super();

    this.webSocketUrl = webSocketUrl;
    this.webSocketOptions = {
      transports: ['websocket'],
      'force new connection': true
    };

    this.connected = false;
  }

  connect() {

    const sock = new SockJS(this.webSocketUrl, this.webSocketOptions);

    sock.onopen = () => {
      console.log('onopen');
      this.sock = sock;
      this.connected = true;
      this.emit('ready');
    };

    sock.onmessage = (message) => {
      console.log('onmessage');
      this.emit('message', message);
    };

    sock.onclose = () => {
      console.log('onclose');
      this.emit('close');
      this.sock = null;
      this.connected = false;
    };
  }

  sendMessage(message) {
    this.sock.send(JSON.stringify(message));
  }

  close() {
    if (this.connected) {
      this.sock.close();
    } else {
      throw new Error('SockJS client not connected!');
    }
  }
}

