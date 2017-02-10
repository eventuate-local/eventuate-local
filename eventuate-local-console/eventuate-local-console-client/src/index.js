/**
 * Created by andrew on 11/9/16.
 */
import React from "react";
import ReactDOM from "react-dom";
import { getRoot } from './root';
import { saveState, restoreState } from './utils/stateManagement';

const INVALIDATE_STORAGE_AFTER = 1000 * 60 * 5;

getRoot(restoreState(INVALIDATE_STORAGE_AFTER)).then(({ root, store }) => {
  const reactRoot = window.document.getElementById("root");
  ReactDOM.render(root, reactRoot);

  window.onbeforeunload = () => {
    saveState(store.getState());
  };

});