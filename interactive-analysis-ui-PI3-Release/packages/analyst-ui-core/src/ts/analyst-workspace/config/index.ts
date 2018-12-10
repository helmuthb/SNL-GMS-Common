import * as GoldenLayout from '@gms/golden-layout';

import { environmentConfig, EnvironmentConfig } from './environment-config';
import { componentList, ComponentList, defaultGoldenLayoutConfig } from './golden-layout-config';
import { systemConfig, SystemConfig } from './system-config';
import { userPreferences, UserPreferences } from './user-preferences';

export interface AnalystUiConfig {
    components: ComponentList;
    workspace: GoldenLayout.Config;
    userPreferences: UserPreferences;
    environment: EnvironmentConfig;
    systemConfig: SystemConfig;
}

export const analystUiConfig: AnalystUiConfig = {
    components: componentList,
    workspace: defaultGoldenLayoutConfig,
    userPreferences,
    environment: environmentConfig,
    systemConfig
};

export { userPreferences, UserPreferences, QcMaskDisplayFilters } from './user-preferences';
export { environmentConfig, EnvironmentConfig } from './environment-config';
export { systemConfig, SystemConfig } from './system-config';
