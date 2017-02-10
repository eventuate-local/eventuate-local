/**
 * Created by andrew on 11/18/16.
 */
export const timestamp = () => ({ timestamp: new Date() - 0 });

export const parseTimestamp = (input) => parseInt(input.split('-')[0], 16);