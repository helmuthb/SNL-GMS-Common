import * as express from 'express';
import * as bodyParser from 'body-parser';
import * as path from 'path';
import * as config from 'config';
import * as workflowClient from './workflow/workflow-client';
import * as signalDetectionClient from './signal-detection/signal-detection-client';
import * as waveformClient from './waveform/waveform-client';
import { graphqlExpress, graphiqlExpress } from 'apollo-server-express';
import { execute, subscribe } from 'graphql';
import { createServer } from 'http';
import { SubscriptionServer } from 'subscriptions-transport-ws';
import { schema } from './schema';

// using the gateway logger
import { gatewayLogger as logger } from './log/gateway-logger';
const objectPath = path.relative(process.cwd(), __filename);

// Load configuration settings
const gqlConfig = config.get('server.graphql');

// Create the GraphQL server
const server = express();

// Register root path
logger.info('register /', {module: objectPath});
server.get('/', (req, res) => {
    res.send('Hello World!');
});

logger.info('register /waveforms');
server.get('/waveforms', waveformClient.waveformSegmentRequestHandler);

// Register GraphQL endpoint
const graphqlPath = gqlConfig.http.graphqlPath;
const parserLimitSize = 1024*1024*2000; // 2 gig
logger.info(`register ${graphqlPath}`);
server.use(
    graphqlPath,
    bodyParser.json({limit: parserLimitSize, type: 'application/json'}),
    graphqlExpress({
        schema
    })
);

// GraphQL HTTP server port
const httpPort = gqlConfig.http.port;
// GraphQL Websocket port
const wsPort = gqlConfig.ws.port;

// Register the GraphiQL endpoint
const graphiqlPath = gqlConfig.http.graphiqlPath;
logger.info(`register ${graphiqlPath}`);
server.use(graphiqlPath, graphiqlExpress({
    endpointURL: 'graphql',
    // Include the subscription endpoint in the GraphiQL configuration
    // (needed in order for GraphiQL to support subscriptions)
    subscriptionsEndpoint: `ws://${gqlConfig.ws.host}:${wsPort}${gqlConfig.ws.path}`
}));

// Listen for GraphQL requests over HTTP
server.listen(httpPort, () => { logger.info(`listening on port ${httpPort}`); });

// Create the Websocket server supporting GraphQL subscriptions
const websocketServer = createServer((request, response) => {
    const responseCode = 404;
    response.writeHead(responseCode);
    response.end();
  });

// Listen for GraphQL subscription connections
websocketServer.listen(wsPort, () => logger.info(
    `Websocket Server is listening on port ${wsPort}`
  ));

// Create the subscription server
SubscriptionServer.create(
      {
        schema,
        execute,
        subscribe,
      },
      {
        server: websocketServer,
        path: gqlConfig.ws.path,
      },
  );

// Initialize processors as needed
workflowClient.start();
waveformClient.start();
signalDetectionClient.start();
logger.info('intervalService started', {module: objectPath});
