/**
 * Created by andrew on 12/2/16.
 */
import mysql from 'promise-mysql';
import invariant from 'invariant';

invariant(process.env.SPRING_DATASOURCE_URL, 'set value for SPRING_DATASOURCE_URL environment variable');
invariant(process.env.SPRING_DATASOURCE_USERNAME, 'set value for SPRING_DATASOURCE_USERNAME environment variable');
invariant(process.env.SPRING_DATASOURCE_PASSWORD, 'set value for SPRING_DATASOURCE_PASSWORD environment variable');

const { host, port, database } = parseJdbcUrl(process.env.SPRING_DATASOURCE_URL);

const pool = mysql.createPool({
  host,
  port,
  user: process.env.SPRING_DATASOURCE_USERNAME,
  password : process.env.SPRING_DATASOURCE_PASSWORD,
  connectionLimit: 10,
  database,
});

export const getConnection = () => {
  return pool.getConnection().disposer((connection) => {
    pool.releaseConnection(connection);
  })
};

function parseJdbcUrl(dataSourceURL) {

  const pattern = new RegExp('jdbc:mysql://([^:/]+)(:[0-9]+)?/(.+$)');
  const matches = pattern.exec(dataSourceURL);

  if (matches) {

    const [ , host, port, database ] = matches;

    return { host, port: (!port) ? 3306 : parseInt(port.replace(':', '')), database };

  } else {
    throw new Error(`Can not parse JDBC URL: ${dataSourceURL}`);
  }

}
