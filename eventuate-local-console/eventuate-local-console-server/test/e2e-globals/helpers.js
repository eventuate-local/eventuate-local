/**
 * Created by andrew on 2/6/17.
 */
import fetch from 'isomorphic-fetch';

const todoListAppUrl = `http://${process.env.DOCKER_HOST_IP}:8080/todos`;

export function initiateEvent() {

  const todo = {
    completed: false,
    order: 2,
    title: "Test #3"
  };

  return fetch(todoListAppUrl, {
    method: 'post',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(todo)
  });

}