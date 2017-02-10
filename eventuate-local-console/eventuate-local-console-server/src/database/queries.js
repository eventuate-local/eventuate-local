/**
 * Created by andrew on 12/2/16.
 */
import bPromise from 'bluebird';
import { getConnection } from './connection';

const query = (sqlQuery, params, process) => bPromise.using(getConnection(), function(connection) {
  return connection.query(sqlQuery, params).then((rows) => {
    return process(rows);
  });
});

function processTypes(rows) {
  return rows;
}

function processInstances(rows) {
  return rows.map(row => ({
    ...row,
    created: parseInt(row.created, 10),
    updated: parseInt(row.updated, 10)
  }));
}

function processEvents(rows) {
  return rows.map(row => ({
    ...row,
    eventData: JSON.parse(row.eventData)
  }));
}

export const getTypes = () => {
  return query('SELECT DISTINCT `entity_type` AS name FROM `entities`', null, processTypes);
};

/**
 *  */
export const getInstances = (typeName) => {
  return query(`SELECT 
      entities.entity_id AS aggregateId,
      entities.entity_type AS type,
      entities.entity_version,
      CONV(SUBSTRING_INDEX(MIN(events.event_id), '-', 1), 16, 10) as created,
      CONV(SUBSTRING_INDEX(entities.entity_version, '-', 1), 16, 10) as updated,
      COUNT(1) as events
    FROM entities
    JOIN events ON entities.entity_id = events.entity_id
    WHERE entities.entity_type = ?
    GROUP BY events.entity_id
    ORDER BY aggregateId`,
    [ typeName ],
    processInstances);
};

export const getEvents = (typeName, instanceId) => {
  // `event_type` = ? AND
  return query('SELECT `event_id` AS eventId, `event_type` AS eventType, `event_data` AS eventData FROM `events` WHERE `entity_id` = ?', [ instanceId ],
    processEvents);
};
