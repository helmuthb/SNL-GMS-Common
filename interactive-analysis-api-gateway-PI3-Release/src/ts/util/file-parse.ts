/**
 * Utility functions for handling CSV files.
 */

 // TODO csv-parse does not seem to work without require-style import
import parseSync = require('csv-parse/lib/sync');
import * as fs from 'fs';

/**
 * Reads the provided source CSV file into memory
 * @param filename The CSV filename from which to read the CSV content
 */
export function readCsvData(csvFilePath: string): any[] {
    const fileContents = fs.readFileSync(csvFilePath, 'utf8');
    const records = parseSync(fileContents, {columns: true, delimiter: '\t'});
    return records;
}

/**
 * Reads the provided source JSON file into memory
 * @param jsonFilePath The JSON filename from which to read the JSON content
 */
export function readJsonData(jsonFilePath: string): any[] {
    const fileContents = fs.readFileSync(jsonFilePath, 'utf8');
    const records = JSON.parse(fileContents);
    return records;
}
