/**
 * Created by andrew on 12/26/16.
 */
import type from 'istypes';
import { defaultMemoize } from 'reselect';
import { simpleHash } from './hash'

export const prepareMessage = defaultMemoize((message) => {
  const parsed = parseMessageRecursively(message);
  const stringified = JSON.stringify(message);
  return {
    id: simpleHash(stringified),
    stringified,
    raw: message,
    parsed
  };
});

function parseMessageRecursively(input) {
  if (type.isObject(input)) {
    return Object.keys(input).reduce((result, k) => {
      result[k] = parseMessageRecursively(input[k]);
      return result;
    }, {})
  } else if (type.isString(input)) {
    try {
      return parseMessageRecursively(JSON.parse(input));
    } catch(ex) {
      return input;
    }
  } else if (type.isArray(input)) {
    return input.map(parseMessageRecursively);
  } else if (type.isFunction(input)) {
    return null;
  }
  return input;
}
