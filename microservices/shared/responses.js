const defaultHeaders = {
  'Content-Type': 'application/json',
  'Access-Control-Allow-Origin': process.env.CORS_ALLOW_ORIGIN || '*',
  'Access-Control-Allow-Headers': 'Authorization,Content-Type',
  'Access-Control-Allow-Methods': 'GET,POST,OPTIONS'
};

const response = (statusCode, body) => ({
  statusCode,
  headers: defaultHeaders,
  body: JSON.stringify(body)
});

const ok = (body) => response(200, body);
const created = (body) => response(201, body);
const badRequest = (message) => response(400, { message });
const unauthorized = (message = 'Unauthorized') => response(401, { message });
const forbidden = (message = 'Forbidden') => response(403, { message });
const internalServerError = () => response(500, { message: 'Internal server error' });

module.exports = {
  ok,
  created,
  badRequest,
  unauthorized,
  forbidden,
  internalServerError
};
