import { isEqual } from 'lodash';

const HotKeySeparator = '+';

const Meta      = 'Meta';
const Control   = 'Control';
const Alt       = 'Alt';
const Shift     = 'Shift';

/**
 * Returns the Hot Key array based on the KeyboardEvent.
 * 
 * @param event the keyboard event
 */
export const getHotKeyArray = (event: KeyboardEvent) => {
    const hotKeyArray: string[] = [];

    if (event.metaKey) {
        hotKeyArray.push(Meta);
    }

    if (event.ctrlKey) {
        hotKeyArray.push(Control);
    }

    if (event.altKey) {
        hotKeyArray.push(Alt);
    }

    if (event.shiftKey) {
        hotKeyArray.push(Shift);
    }

    // add non-control characters
    // see: https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values
    if (event.key !== Meta && event.key !== Control && event.key !== Alt && event.key !== Shift) {
        hotKeyArray.push(event.key);
    }

    return hotKeyArray;
};

/**
 * Returns the Hot Key string based on the KeyboardEvent.
 * 
 * @param event the keyboard event
 */
export const getHotKeyString = (event: KeyboardEvent) => getHotKeyString(event)
    .join(HotKeySeparator);

/**
 * Returns true if the hotkey command is satisfied. False otherwise.
 * 
 * @param event the keyboard event
 * @param hotKeyCommand the hotkey command
 */
export const isHotKeyCommandSatisfied = (event: KeyboardEvent, hotKeyCommand: string) => {
    if (!event) {
        return false;
    }

    if (!hotKeyCommand) {
        return false;
    }

    // remove all whitespace
    hotKeyCommand.replace(/\s/g, '');

    const hotKeyArray = getHotKeyArray(event);
    const commandArray = hotKeyCommand.split(HotKeySeparator);

    return isEqual(hotKeyArray.sort(), commandArray.sort());
};
