import { Router } from 'express';
import bodyParser from 'body-parser';
import * as DB from '../database/queries';
import { recordSub, lookupSub } from '../services/subscriptions';
import { getLogger } from '../utils/logger';

const logger = getLogger();
const router = Router();
const jsonParser = bodyParser.json();

const safeStringify = (o) => {
  let cache = [];
  const result = JSON.stringify(o, function(key, value) {
    if (typeof value === 'object' && value !== null) {
      if (cache.indexOf(value) !== -1) {
        // Circular reference found, discard key
        return;
      }
      // Store value in our collection
      cache.push(value);
    }
    return value;
  });
  cache = null; // Enable garbage collection
  return result;
};

const noCache = res => {
  // Disable caching for content files
  res.header("Cache-Control", "no-cache, no-store, must-revalidate");
  res.header("Pragma", "no-cache");
  res.header("Expires", 0);
};

const jsonResponse = res => {
  // 'Content-Type', 'application/json'
  res.header('Content-Type', 'application/json');
};

const errorHandlerFor = (req, res) => (err) => {
  // res.text().then(txt => console.log(txt));
  if (err.code) {
    switch (err.code) {
      case 'ECONNREFUSED':
        return Promise.resolve({
          error: { ...err }
        });
    }
  }
  logger.error(safeStringify(err));
  throw err;
};

router.get('/', (req, res, next) => {
  res.json({ message: 'hooray! welcome to our api!' });
});

router.get('/types', (req, res, next) => {
  noCache(res);

  DB.getTypes()
    .catch(errorHandlerFor(req, res))
    .then(items => items.error ? res.json(items) : res.json({ items }));
});

router.get('/instances/:typeName', (req, res, next) => {
  noCache(res);

  DB.getInstances(req.params['typeName'])
    .catch(errorHandlerFor(req, res))
    .then(items => items.error ? res.json(items) : res.json({
      offset: 0, // /instances/:typeName?offset=,
      limit: 25, // /instances/:typeName?offset=..&limit=
      items
    }), errorHandlerFor(req, res));
});

router.get('/instance/:typeName/:instanceId', (req, res, next) => {
  noCache(res);

  DB.getEvents(req.params['typeName'], req.params['instanceId'])
    .catch(errorHandlerFor(req, res))
    .then(items => items.error ? res.json(items) : res.json({ items }));
});

/**
 * Register key for the topics set
 */
router.post('/subscription', jsonParser, (req, res, next) => {

  const topics = req.body;
  logger.debug('topics:', topics);

  if (Array.isArray(topics) && topics.length > 0) {
    const key = recordSub(req.body);
    logger.debug(`key: ${key}`);
    res.status(200).json({ key, items: req.body });
  } else {
    res.status(400).json( { err: 'Incorrect topics array' });
  }

});

/**
 * Retrieve the key-items pair
 */
router.get('/subscription/:subKey', (req, res, next) => {
  noCache(res);

  const key = req.params['subKey'];
  const items = lookupSub(key);
  res.json({ key, items });
});

export default router;
