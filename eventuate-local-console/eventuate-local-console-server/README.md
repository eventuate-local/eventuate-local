# Eventuate Server Console - Frontend & API Gateway Server (NodeJs)

## Installation

```bash
npm i
```

or (if using `yarn`):

```bash
yarn
```

## Prepare the server:

### Environment variables:

Duplicate the `.env.sample` file and rename it to `.env`. Then edit it and set the right values for 

```bash
EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
SPRING_DATASOURCE_URL=
```

or, alternatively,

set them manually in the environment:

```bash
export EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING=..
export SPRING_DATASOURCE_USERNAME=..
export SPRING_DATASOURCE_PASSWORD=..
export SPRING_DATASOURCE_URL=..
```

## Run the dev server:


```bash
npm run start
```

## Run the dev server with the watch support:


```bash
npm run start:watch
```

## Build for the production server:


```bash
npm run build
```

## Run the production server:

Set the port for the site:

```bash
export PORT=8080
```

and then..

```bash
npm run serve
```
Then navigate to `http://localhost:8080` in a browser.


## Running with Docker

Enter `docker` directory:
```bash
cd docker
```
Create application code archive:
```bash
./build.sh
```
Enter project root directory:
```bash
cd ../../../
```
Rebuild `consoleserver` image:
```bash
docker-compose -f docker-compose-eventuate-local.yml build consoleserver
```
Run Eventuate Local:
```bash
docker-compose -f docker-compose-eventuate-local.yml up
```

### Running e2e tests with docker:

```bash
npm run test-e2e-docker
```

OR for running on a headless server:

```bash
npm run test-e2e-docker-headless
```
Note: you need to have Xvfb and Firefox installed on the headless server

## Updating Client App code

There's rarely a need to update the Client App. The code is already prepackaged and bundled in the `/public` folder. However, if there's a need to do so, a developer fist needs to update the app inside the `eventuate-local-console-client/src` folder and use `npm run start-dev` (in `eventuate-local-console-client`) to control how well the code works. Then, `npm run build` (in `eventuate-local-console-client`) to prepackage the app inside the client app folder.

After all this is node, in the `eventuate-local-console-server` run:

```bash
npm run static
```
