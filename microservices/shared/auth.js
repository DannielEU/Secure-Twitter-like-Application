const jwt = require('jsonwebtoken');
const jwksClient = require('jwks-rsa');

class HttpError extends Error {
  constructor(statusCode, message) {
    super(message);
    this.statusCode = statusCode;
  }
}

const jwksClients = new Map();

const normalizeIssuer = (issuerUri) => {
  if (!issuerUri || typeof issuerUri !== 'string') {
    throw new HttpError(500, 'AUTH0_ISSUER_URI is not configured');
  }

  return issuerUri.endsWith('/') ? issuerUri : `${issuerUri}/`;
};

const getJwksClient = (issuerUri) => {
  const normalizedIssuer = normalizeIssuer(issuerUri);
  if (jwksClients.has(normalizedIssuer)) {
    return jwksClients.get(normalizedIssuer);
  }

  const client = jwksClient({
    jwksUri: `${normalizedIssuer}.well-known/jwks.json`,
    cache: true,
    cacheMaxEntries: 5,
    cacheMaxAge: 10 * 60 * 1000
  });

  jwksClients.set(normalizedIssuer, client);
  return client;
};

const getSigningKey = async (issuerUri, kid) => {
  const client = getJwksClient(issuerUri);
  const key = await client.getSigningKey(kid);
  return key.getPublicKey();
};

const getTokenFromHeader = (authorizationHeader) => {
  if (!authorizationHeader || !authorizationHeader.startsWith('Bearer ')) {
    throw new HttpError(401, 'Missing Bearer access token');
  }
  return authorizationHeader.replace('Bearer ', '').trim();
};

const extractScopes = (claims) => {
  const scopeClaim = typeof claims.scope === 'string' ? claims.scope : '';
  return scopeClaim.split(/\s+/).filter(Boolean);
};

const verifyAccessToken = async (
  authorizationHeader,
  { requiredScopes = [] } = {}
) => {
  const issuer = normalizeIssuer(process.env.AUTH0_ISSUER_URI);
  const audience = process.env.AUTH0_AUDIENCE;

  if (!audience) {
    throw new HttpError(500, 'AUTH0_AUDIENCE is not configured');
  }

  const token = getTokenFromHeader(authorizationHeader);
  const decoded = jwt.decode(token, { complete: true });

  if (!decoded?.header?.kid) {
    throw new HttpError(401, 'Invalid JWT header');
  }

  const publicKey = await getSigningKey(issuer, decoded.header.kid);
  let claims;

  try {
    claims = jwt.verify(token, publicKey, {
      algorithms: ['RS256'],
      issuer,
      audience
    });
  } catch (_error) {
    throw new HttpError(401, 'Invalid or expired access token');
  }

  const tokenScopes = extractScopes(claims);
  const missingScopes = requiredScopes.filter((scope) => !tokenScopes.includes(scope));

  if (missingScopes.length > 0) {
    throw new HttpError(403, `Missing required scope(s): ${missingScopes.join(', ')}`);
  }

  return {
    claims,
    scopes: tokenScopes
  };
};

module.exports = {
  HttpError,
  verifyAccessToken
};
