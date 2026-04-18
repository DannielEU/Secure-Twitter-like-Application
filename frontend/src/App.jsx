import { useEffect, useMemo, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { apiConfig } from './config';

function App() {
  const {
    loginWithRedirect,
    logout,
    user,
    isAuthenticated,
    isLoading,
    getAccessTokenSilently,
    error
  } = useAuth0();

  const [posts, setPosts] = useState([]);
  const [newPost, setNewPost] = useState('');
  const [profile, setProfile] = useState(null);
  const [appError, setAppError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const apiBaseUrl = useMemo(() => apiConfig.baseUrl.replace(/\/$/, ''), []);

  const fetchPosts = async () => {
    try {
      const response = await fetch(`${apiBaseUrl}/api/posts`);
      if (!response.ok) {
        throw new Error(`Could not load posts (${response.status})`);
      }
      setPosts(await response.json());
    } catch (fetchError) {
      setAppError(fetchError.message);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, []);

  const fetchProfile = async () => {
    setAppError('');
    try {
      const token = await getAccessTokenSilently();
      const response = await fetch(`${apiBaseUrl}/api/me`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error(`Could not load profile (${response.status})`);
      }

      setProfile(await response.json());
    } catch (profileError) {
      setAppError(profileError.message);
    }
  };

  const handleCreatePost = async (event) => {
    event.preventDefault();
    if (!newPost.trim()) {
      return;
    }

    setSubmitting(true);
    setAppError('');

    try {
      const token = await getAccessTokenSilently();
      const response = await fetch(`${apiBaseUrl}/api/posts`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ content: newPost })
      });

      if (!response.ok) {
        const errorBody = await response.json().catch(() => ({}));
        const message = errorBody.message || `Could not create post (${response.status})`;
        throw new Error(message);
      }

      setNewPost('');
      await fetchPosts();
    } catch (createError) {
      setAppError(createError.message);
    } finally {
      setSubmitting(false);
    }
  };

  const remainingChars = 140 - newPost.length;

  return (
    <div className="page-shell">
      <header className="hero">
        <p className="eyebrow">SECURE MICROBLOG</p>
        <h1>Streamline</h1>
        <p className="hero-copy">
          Publish short authenticated posts into a single global stream secured by Auth0 + JWT.
        </p>
        <div className="auth-controls">
          {!isAuthenticated ? (
            <button className="button button-primary" onClick={() => loginWithRedirect()}>
              Log In
            </button>
          ) : (
            <>
              <button className="button" onClick={fetchProfile}>
                Load /api/me
              </button>
              <button
                className="button button-primary"
                onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
              >
                Log Out
              </button>
            </>
          )}
        </div>
      </header>

      {isLoading && <p className="notice">Authenticating session...</p>}
      {error && <p className="notice error">Auth0 error: {error.message}</p>}
      {appError && <p className="notice error">{appError}</p>}

      {isAuthenticated && (
        <section className="card">
          <h2>Create post</h2>
          <p className="muted">Logged in as {user?.name || user?.nickname || user?.sub}</p>
          <form onSubmit={handleCreatePost}>
            <textarea
              value={newPost}
              onChange={(event) => setNewPost(event.target.value)}
              maxLength={140}
              placeholder="What do you want to share with the stream?"
              rows={4}
            />
            <div className="form-footer">
              <span className={remainingChars < 0 ? 'error-text' : 'muted'}>
                {remainingChars} characters left
              </span>
              <button className="button button-primary" type="submit" disabled={submitting}>
                {submitting ? 'Posting...' : 'Publish'}
              </button>
            </div>
          </form>
        </section>
      )}

      {profile && (
        <section className="card">
          <h2>Current user profile (/api/me)</h2>
          <pre>{JSON.stringify(profile, null, 2)}</pre>
        </section>
      )}

      <section className="card">
        <h2>Public stream</h2>
        <ul className="post-list">
          {posts.map((post) => (
            <li key={post.id} className="post-item">
              <div className="post-meta">
                <strong>{post.authorName}</strong>
                <span>{new Date(post.createdAt).toLocaleString()}</span>
              </div>
              <p>{post.content}</p>
            </li>
          ))}
          {posts.length === 0 && <li className="muted">No posts yet.</li>}
        </ul>
      </section>
    </div>
  );
}

export default App;
