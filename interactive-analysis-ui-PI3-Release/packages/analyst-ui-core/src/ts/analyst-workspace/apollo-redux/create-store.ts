
import * as Redux from 'redux';
import { electronEnhancer } from 'redux-electron-store';
declare var require;
// tslint:disable-next-line:no-implicit-dependencies no-require-imports no-var-requires
const electron = require('electron');

import { AnalystWorkspaceState } from '../state';
import { Reducer } from '../state/reducer';

// @ts-ignore
export const createStore = (): Redux.Store<any> => {

    let windowIsDefined: boolean;
    try {
        windowIsDefined = Boolean(window);
    } catch (e) {
        windowIsDefined = false;
    }

    // @ts-ignore
    const store = Redux.createStore(
        Redux.combineReducers<AnalystWorkspaceState>({
            app: Reducer,
        }),
        // @ts-ignore
        electron ?
            Redux.compose(
                electron ? electronEnhancer({
                    filter: windowIsDefined ?
                        undefined
                        : {
                            apollo: false
                        },
                    dispatchProxy: a => store.dispatch(a),
                }) : undefined,
            )
            :
            Redux.compose(
                Redux.applyMiddleware()
            )
    );

    return store;
};
