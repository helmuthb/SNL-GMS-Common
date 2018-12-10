import { gatewayLogger as logger } from '../log/gateway-logger';

/**
 * Resolvers for the common API gateway
 */

// GraphQL Resolvers
logger.info('Creating common API Gateway GraphQL resolvers...');
export const resolvers = {

    // Field resolvers for Timeseries
    Timeseries: {
        /**
         * Special interface resolver to determine the implementing type based on field content
         */
        __resolveType(obj, context, info) {
            if (obj.waveformSamples) {
                return 'Waveform';
            }
            return undefined;
        }
    }
};
