const { randomUUID } = require('crypto');
const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, PutCommand, ScanCommand } = require('@aws-sdk/lib-dynamodb');
const { HttpError, verifyAccessToken } = require('../shared/auth');
const responses = require('../shared/responses');

const dbClient = DynamoDBDocumentClient.from(new DynamoDBClient({}));
const POSTS_TABLE = process.env.POSTS_TABLE;

const parseBody = (body) => {
  try {
    return body ? JSON.parse(body) : {};
  } catch (_error) {
    throw new HttpError(400, 'Request body must be valid JSON');
  }
};

const validateContent = (content) => {
  const normalized = typeof content === 'string' ? content.trim() : '';

  if (!normalized) {
    throw new HttpError(400, 'content is required');
  }

  if (normalized.length > 140) {
    throw new HttpError(400, 'content must not exceed 140 characters');
  }

  return normalized;
};

const sortByDateDesc = (posts) => posts.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

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

exports.listPosts = async () => {
  try {
    const result = await dbClient.send(new ScanCommand({ TableName: POSTS_TABLE }));
    const posts = sortByDateDesc(result.Items || []);
    return responses.ok(posts);
  } catch (error) {
    return mapError(error);
  }
};

exports.createPost = async (event) => {
  try {
    const authorizationHeader = event.headers?.authorization || event.headers?.Authorization;
    const { claims } = await verifyAccessToken(authorizationHeader, {
      requiredScopes: ['write:posts']
    });

    const body = parseBody(event.body);
    const content = validateContent(body.content);

    const post = {
      id: randomUUID(),
      authorId: claims.sub,
      authorName: claims.nickname || claims.name || claims.sub,
      content,
      createdAt: new Date().toISOString()
    };

    await dbClient.send(
      new PutCommand({
        TableName: POSTS_TABLE,
        Item: post
      })
    );

    return responses.created(post);
  } catch (error) {
    return mapError(error);
  }
};
