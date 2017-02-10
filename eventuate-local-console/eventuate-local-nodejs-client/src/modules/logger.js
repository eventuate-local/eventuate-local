import log4js from 'log4js';

export const getLogger = ({ logLevel, title } = {}) => {

  const logger = log4js.getLogger(title || 'Logger');

  if (!logLevel) {
    logLevel = (process.env.NODE_ENV !== 'production')?'DEBUG':'ERROR';
  }

  logger.setLevel(logLevel);

  return logger;

};