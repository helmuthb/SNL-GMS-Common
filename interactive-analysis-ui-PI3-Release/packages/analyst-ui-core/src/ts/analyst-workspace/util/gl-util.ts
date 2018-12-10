import * as Gl from '@gms/golden-layout';
import * as lodash from 'lodash';
import * as React from 'react';

/**
 * Attaches an event handler to the golden-layout event 'show' that will force 
 * the component to update when dispatched. 
 * 
 * @param glContainer the golden-layout container
 * @param component the react component to force update
 */
export const addGlForceUpdateOnShow = (glContainer: Gl.Container, component: React.Component) => {
    if (glContainer) {
        // when component is brought to the foreground
        // by golden-layout (visible), do a force redraw
        glContainer.on('show', () => {
            lodash.defer(() => {
                component.forceUpdate();
            });
        });
    }
};

/**
 * Attaches an event handler to the golden-layout event 'resize' that will force 
 * the component to update when dispatched. 
 * 
 * @param glContainer the golden-layout container
 * @param component the react component to force update
 */
export const addGlForceUpdateOnResize = (glContainer: Gl.Container, component: React.Component) => {
    if (glContainer) {
        // force update when the golden-layout cotainer is resized
        glContainer.on('resize', () => {
            component.forceUpdate();
        });
    }
};
