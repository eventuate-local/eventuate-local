/**
 * Created by andrew on 11/18/16.
 */
import Immutable from 'immutable';

const jsDecorator = (fn, inputOnly) => {
  return (state, ...args) => {
    return inputOnly ?
      fn(...[Immutable.fromJS(state), ...args]) :
      toJS(fn(...[Immutable.fromJS(state), ...args]));
  };
};

function toJS(input) {
  // if (!input) {
  //   debugger;
  // }
  if (input && typeof input.toJS == 'function') {
    return input.toJS();
  }
  return input;
}

const updateState = (state, props, fn) => {
  return state.updateIn(props, fn)
};

const readState = (state, props, defaults) => {
  return state.getIn(props, defaults);
};

export const updateStateJs = jsDecorator(updateState);
export const readStateJs = jsDecorator(readState, true);
export const readStateJsForce = jsDecorator(readState);
