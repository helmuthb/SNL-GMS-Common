/**
 * Global GraphQL schema definition for the entire API gateway
 */
import { merge } from 'lodash';
import * as path from 'path';
import { makeExecutableSchema } from 'graphql-tools';
import { schema as commonSchema } from './common/schema';
import { resolvers as commonResolvers } from './common/resolvers';
import { schema as workflowSchema } from './workflow/schema';
import { resolvers as workflowResolvers } from './workflow/resolvers';
import { schema as stationSchema } from './station/schema';
import { resolvers as stationResolvers } from './station/resolvers';
import { schema as waveformSchema } from './waveform/schema';
import { resolvers as waveformResolvers } from './waveform/resolvers';
import { schema as signalDetectionSchema } from './signal-detection/schema';
import { resolvers as signalDetectionResolvers } from './signal-detection/resolvers';
import { schema as eventSchema } from './event/schema';
import { resolvers as eventResolvers } from './event/resolvers';
import { schema as qcMaskSchema } from './qc-mask/schema';
import { resolvers as qcMaskResolvers } from './qc-mask/resolvers';
import { schema as fkSchema } from './fk/schema';
import { resolvers as fkResolvers } from './fk/resolvers';

// using the gateway logger
import { gatewayLogger as logger } from './log/gateway-logger';
const objectPath = path.relative(process.cwd(), __filename);

// GraphQL schema definitions
logger.info('Creating graphql schema...', {module: objectPath});

const typeDefs = [commonSchema, workflowSchema, stationSchema, waveformSchema,
    signalDetectionSchema, eventSchema, qcMaskSchema, fkSchema];

// Merge GraphQL resolvers from the schema domains
let resolvers = merge(commonResolvers,
                      workflowResolvers,
                      stationResolvers,
                      waveformResolvers,
                      signalDetectionResolvers);

resolvers = merge(resolvers,
                  eventResolvers,
                  qcMaskResolvers,
                  fkResolvers);

// Build the GraphQL schema
export const schema = makeExecutableSchema({
    typeDefs,
    resolvers
});
