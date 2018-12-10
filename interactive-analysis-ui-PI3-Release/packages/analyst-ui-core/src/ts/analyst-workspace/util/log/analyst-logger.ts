import * as log4javascript from 'log4javascript';

// Create the logger
// export const AnalystLogger = log4javascript.getDefaultLogger();
export const AnalystLogger = log4javascript.getLogger('analyst-logger');

// Create a PopUpAppender with default options
const popUpAppender = new log4javascript.PopUpAppender();
// Change the desired configuration options
popUpAppender.setNewestMessageAtTop(true);
popUpAppender.setComplainAboutPopUpBlocking(true);
popUpAppender.setUseOldPopUp(true);
popUpAppender.setReopenWhenClosed(true);
popUpAppender.setScrollToLatestMessage(true);
popUpAppender.setFocusPopUp(false);
popUpAppender.setInitiallyMinimized(true);

export const showLogPopup = () => {
    popUpAppender.show();
};

// Add the appender to the logger
AnalystLogger.addAppender(popUpAppender);
AnalystLogger.setLevel(log4javascript.Level.ALL);
popUpAppender.hide();
