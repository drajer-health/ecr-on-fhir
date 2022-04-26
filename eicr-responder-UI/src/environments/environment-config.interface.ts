export interface EnvironmentConfig {
  environment: {
    baseUrl: string;
  };
}

export const ENV_CONFIG = new InjectionToken<EnvironmentConfig>('EnvironmentConfig');