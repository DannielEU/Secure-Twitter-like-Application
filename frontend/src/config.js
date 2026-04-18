const readEnv = (key, fallback = '') => {
  const value = import.meta.env[key] ?? fallback;
  if (!value) {
    console.warn(`Missing environment variable: ${key}`);
  }
  return value;
};

export const authConfig = {
  domain: readEnv('VITE_AUTH0_DOMAIN'),
  clientId: readEnv('VITE_AUTH0_CLIENT_ID'),
  audience: readEnv('VITE_AUTH0_AUDIENCE')
};

export const apiConfig = {
  baseUrl: readEnv('VITE_API_BASE_URL', 'http://localhost:8080')
};
