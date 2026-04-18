const { HttpError, verifyAccessToken } = require('../shared/auth');
const responses = require('../shared/responses');

const mapError = (error) => {
  if (error instanceof HttpError) {
    if (error.statusCode === 401) {
      return responses.unauthorized(error.message);
    }
    if (error.statusCode === 403) {
      return responses.forbidden(error.message);
    }
    return responses.badRequest(error.message);
  }

  console.error(error);
  return responses.internalServerError();
};

exports.getMe = async (event) => {
  try {
    const authorizationHeader = event.headers?.authorization || event.headers?.Authorization;
    const { claims, scopes } = await verifyAccessToken(authorizationHeader, {
      requiredScopes: ['read:profile']
    });

    return responses.ok({
      id: claims.sub,
      name: claims.name || claims.nickname || claims.sub,
      email: claims.email || null,
      scopes
    });
  } catch (error) {
    return mapError(error);
  }
};
