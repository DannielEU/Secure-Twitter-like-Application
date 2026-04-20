const { verifyAccessToken } = require('./auth');

const extractTokenFromHeader = (headers) => {
    const authHeader = headers?.authorization || headers?.Authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return null;
    }
    return authHeader;
};

exports.handler = async (event) => {
    console.log('Authorizer event:', JSON.stringify(event, null, 2));

    try {
        const token = extractTokenFromHeader(event.headers);

        if (!token) {
            console.log('No token provided');
            return generatePolicy('user', 'Deny', event.routeArn);
        }

        const { claims, scopes } = await verifyAccessToken(token);

        const method = event.requestContext?.http?.method || event.method;
        const path = event.requestContext?.http?.path || event.path;

        let requiredScopes = [];
        if (path === '/api/posts' && method === 'POST') {
            requiredScopes = ['write:posts'];
        } else if (path === '/api/me' && method === 'GET') {
            requiredScopes = ['read:profile'];
        }

        for (const scope of requiredScopes) {
            if (!scopes.includes(scope)) {
                console.log(`Missing scope: ${scope}, user has: ${scopes}`);
                return generatePolicy(claims.sub, 'Deny', event.routeArn);
            }
        }

        console.log(`Authorization successful for user: ${claims.sub}`);

        return generatePolicy(claims.sub, 'Allow', event.routeArn, {
            userId: claims.sub,
            userEmail: claims.email || '',
            scopes: scopes.join(' ')
        });
    } catch (error) {
        console.error('Authorization failed:', error);
        return generatePolicy('user', 'Deny', event.routeArn);
    }
};


const generatePolicy = (principalId, effect, resource, context = {}) => {
    return {
        principalId,
        policyDocument: {
            Version: '2012-10-17',
            Statement: [
                {
                    Action: 'execute-api:Invoke',
                    Effect: effect,
                    Resource: resource
                }
            ]
        },
        context
    };
};