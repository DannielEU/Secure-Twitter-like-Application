import React from 'react';
import ReactDOM from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import App from './App';
import './styles.css';
import { authConfig } from './config';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Auth0Provider
      domain={authConfig.domain}
      clientId={authConfig.clientId}
      authorizationParams={{
        audience: authConfig.audience,
        redirect_uri: window.location.origin
      }}
      useRefreshTokens
      cacheLocation="memory"
    >
      <App />
    </Auth0Provider>
  </React.StrictMode>
);
