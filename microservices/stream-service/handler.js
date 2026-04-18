const { DynamoDBClient } = require('@aws-sdk/client-dynamodb');
const { DynamoDBDocumentClient, ScanCommand } = require('@aws-sdk/lib-dynamodb');
const responses = require('../shared/responses');

const dbClient = DynamoDBDocumentClient.from(new DynamoDBClient({}));
const POSTS_TABLE = process.env.POSTS_TABLE;

const sortByDateDesc = (posts) => posts.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

exports.getStream = async () => {
  try {
    const result = await dbClient.send(new ScanCommand({ TableName: POSTS_TABLE }));
    const posts = sortByDateDesc(result.Items || []);

    return responses.ok({
      totalPosts: posts.length,
      posts
    });
  } catch (error) {
    console.error(error);
    return responses.internalServerError();
  }
};
