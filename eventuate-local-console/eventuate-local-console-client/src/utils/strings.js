/**
 * Created by andrew on 11/21/16.
 */

export function padStart (text, max, mask) {
  const cur = text.length;
  if (max <= cur) {
    return text;
  }
  const masked = max - cur;
  let filler = String(mask) || ' ';
  while (filler.length < masked) {
    filler += filler;
  }
  const fillerSlice = filler.slice(0, masked);
  return fillerSlice + text;
}

export function uid(...args) {
  return args.map(i => padStart(i.toString(16), 16, '0')).join('-');
}