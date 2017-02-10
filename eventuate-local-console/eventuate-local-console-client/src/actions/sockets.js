/**
 * Created by andrew on 12/14/16.
 */
import SockJS from 'sockjs-client';
import * as N from './network';

let sock = null; // new SockJS(`/comm`);
let link = null; // Promise yes/no

export const wsRequestSubscriptionAsync = ({ key, items }) =>
  (dispatch) => {
    return dispatch(establishWsConnectionAsync())
      .then(() => {
        sock.send(N.wsServerCommand({
          verb: 'subscribe',
          key,
          items
        }));
      })
  };

export const establishWsConnectionAsync = () =>
  (dispatch, getState) => {

    if (sock) {
      return link;
    }

    return (link = new Promise((resolve, reject) => {
      sock = new SockJS(`/comm`);

      sock.send = (function(send, ctx) {
        return function send_upg(message) {
          return send.call(ctx, JSON.stringify(message));
        };
      })(sock.send, sock);

      sock.onopen = () => {
        dispatch(N.wsEstablished());

        sock.send(N.wsServerCommand({
          text: 'listening'
        }));

        resolve();
      };

      /**
       * Event type: "message"
       * @param evt: {}
       * // { data: String, timeStamp: Number }
       */
      sock.onmessage = (evt) => {
        const { data, timeStamp } = evt;
        const payload = getJson(data);
        if (payload) {
          dispatch(N.wsIncomingMessage(payload, timeStamp));
        }
      };

      sock.onclose = (evt) => {
        /*
         bubbles:false
         cancelable:false
         code:1000
         reason:"Normal closure"
         timeStamp:1481672449038
         type:"close"
         wasClean:true
         */
        sock = null;
        const { wasClean, code } = evt;
        if (code >= 2000) {
          dispatch(N.wsNotConnecting(code, reason));
          reject();
          return;
        }
        if (wasClean) {
          dispatch(N.wsTerminated());
        } else {
          dispatch(N.wsLostConnectionAsync());
        }
      };
    }));

  };

export const terminateWsConnectionAsync = () =>
  (dispatch, getState) => {
    // dispatch
    if (!sock) {
      return Promise.resolve();
    }
    return link.then(() => {
      sock.close();
      return Promise.resolve();
    }, () => Promise.resolve())
  };


function getJson(input) {
  try {
    return JSON.parse(input);
  } catch (ex) {}
}