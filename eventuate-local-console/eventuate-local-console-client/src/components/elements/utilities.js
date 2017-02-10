/**
 * Created by andrew on 12/11/16.
 */
import React, { Component } from 'react';

export const breakWords = (input) => input.split(/\b/).reduce((m, s, idx) => {
  if (m.length) {
    m.push(<wbr key={ `${ s }-${ idx }` } />);
  }
  m.push(s);
  return m;
}, []);