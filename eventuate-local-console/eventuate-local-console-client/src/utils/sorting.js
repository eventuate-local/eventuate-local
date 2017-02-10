/**
 * Created by andrew on 11/16/16.
 */
export const defaultSortingIterator = (a = '', b = '') => a.toLowerCase().localeCompare(b.toLowerCase());

export const sortAscIterator = (selector = (K) => K.toString()) =>
  (a, b) => defaultSortingIterator(selector(a), selector(b));

export const sortDescIterator = (selector = (K) => K.toString()) =>
  (b, a) => defaultSortingIterator(selector(a), selector(b));

