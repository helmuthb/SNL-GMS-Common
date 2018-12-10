/**
 * GraphQL schema definition for the Frequency Wavenumber API gateway
 */
import { resolve } from 'path';
import { readFileSync } from 'fs';

// GraphQL schema definitions
export const schema = readFileSync(resolve(process.cwd(), 'resources/graphql/fk/schema.graphql')).toString();
