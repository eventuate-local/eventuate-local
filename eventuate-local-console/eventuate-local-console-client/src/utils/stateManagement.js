/**
 * Created by andrew on 1/23/17.
 */
import Immutable, { Map } from 'immutable';

const LOCAL_STORAGE_KEY = 'react-store';

const [readStrategy, saveStrategy, deleteStrategy] = (() => {
  try {
    return [
      localStorage.getItem.bind(localStorage),
      localStorage.setItem.bind(localStorage),
      localStorage.removeItem.bind(localStorage)
    ];
  } catch (ex) {
    const K = (x) => x;
    return [ K, K, K ];
  }
})();

export const persist = (state) => saveStrategy(LOCAL_STORAGE_KEY, JSON.stringify(state));
export const restore = () => {
  try {
    return JSON.parse(readStrategy(LOCAL_STORAGE_KEY));
  } catch(ex) {}
};

export const archive = (fn) => {
  return (state) => {
    const savingState = state.data || {};
    return fn({
      when: new Date() - 0,
      data: savingState.toJS ? savingState.toJS() : savingState
    });
  };
};

export const dearchive = (fn) =>
  timespan => {
    const { when = null, data = null } = fn() || {};
    if (!when || !data) {
      return;
    }

    const datetime = new Date() - 0;
    if (Math.abs(datetime - when) > timespan) {
      deleteStrategy(LOCAL_STORAGE_KEY);
      return;
    }
    const stateBit = Map(Immutable.fromJS(data));
    return {
      data: stateBit
    };
  };

export const restoreState = dearchive(restore);
export const saveState = archive(persist);