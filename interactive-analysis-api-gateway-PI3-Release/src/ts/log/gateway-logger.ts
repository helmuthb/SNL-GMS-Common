/**
 * provide back-end logger for gateway 
 */

import { Logger, transports } from 'winston';
import * as fs from 'fs';

// TODO daily rotate file does not seem to work without require-style import
const winston = require('winston');
require('winston-daily-rotate-file');

const env = process.env.NODE_ENV || 'development';
const logDir = 'log';
const logFile = 'gateway.log';
const dailyLogFile = 'gateway-daily.log';
const errorLogFile = 'errors.log';

// Create the log directory if it does not exist
if (!fs.existsSync(logDir)) {
  fs.mkdirSync(logDir);
}

// const tsFormat = () => (new Date()).toLocaleTimeString();
const tsUTCFormat = () => (new Date()).toUTCString();
const tsISOFormat = () => (new Date()).toISOString();

const fileMaxNumber = 3;
const fileBaseSize = 1024;
const fileMaxSize = Math.pow(fileBaseSize, 2);

export const gatewayLogger = new (Logger)({
  transports: [
    // colorize the output to the console
    new (transports.Console)({
      timestamp: tsUTCFormat,
      colorize: true,
      prettyPrint: true,
      label: __filename,
      level: 'info'
    }),
    new (transports.File)({
      name: 'debug',
      label: 'Log',
      filename: `${logDir}/${logFile}`,
      timestamp: tsISOFormat,
      maxFiles: fileMaxNumber,
      maxsize: fileMaxSize,
      level: env === 'development' ? 'debug' : 'info'
    }),
    new (transports.File)({
      name: 'error',
      label: 'errorLog',
      filename: `${logDir}/${errorLogFile}`,
      timestamp: tsUTCFormat,
      level: 'error'
    }),
    new (winston.transports.DailyRotateFile)({
      name: 'daily',
      filename: `${logDir}/${dailyLogFile}`,
      datePattern: 'yyyy-MM-dd.',
      prepend: true,
      localTime: false,
      zippedArchive: false,
      maxDays: 7,
      createTree: false,
      level: process.env.ENV === 'development' ? 'debug' : 'info'
    })
    /* new (transports.Http)({
            The Http transport is a generic way to log, query, and stream logs
            from an arbitrary Http endpoint, preferably winstond.
            It takes options that are passed to the node.js http or https request:
            @host: (Default: localhost) Remote host of the HTTP logging endpoint
            @port: (Default: 80 or 443) Remote port of the HTTP logging endpoint
            @path: (Default: /) Remote URI of the HTTP logging endpoint
            @auth: (Default: None) An object representing the username and password for HTTP Basic Auth
            @ssl: (Default: false) Value indicating if we should us HTTPS
      host: 'host',
      port: 9999,
      path: '/',
      auth: someObject,
      ssl: false
    }) */
  ]
});

gatewayLogger.info('Logging to %s/%s', logDir, logFile);
