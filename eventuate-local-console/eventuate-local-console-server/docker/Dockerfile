FROM node:boron
WORKDIR /app
ADD build/eventuate-local-console.tar.gz ./
WORKDIR /app/eventuate-local-console-client
RUN npm install --no-optional
RUN npm install -g webpack
RUN npm run build
WORKDIR /app/eventuate-local-console-server
RUN npm install --no-optional
RUN npm run static
RUN npm run build
CMD node /app/eventuate-local-console-server/dist/index.js
