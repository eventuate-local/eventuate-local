(() => {
  const envVars = [
    'SPRING_DATASOURCE_URL',
    'SPRING_DATASOURCE_USERNAME',
    'SPRING_DATASOURCE_PASSWORD',
    'EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING',
  ];

  const badVars = [];
  envVars.forEach(varName => {
    if (!process.env[varName]) {
      badVars.push(varName);
    }
  });

  if (badVars.length > 0) {
    console.error(`Setup environment variables: ${badVars.join(', ')}`);
    process.exit(1);
  }

})();
