import { GoogleAuth } from 'google-auth-library';
import sa from './service-account.json' with { type: 'json' };
import https from 'https';

const auth = new GoogleAuth({
  credentials: sa,
  scopes: ['https://www.googleapis.com/auth/firebase', 'https://www.googleapis.com/auth/cloud-platform'],
});

const rules = `rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /policies/{policyId} {
      allow read: if true;
    }
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /events/{eventId} {
      allow read, write: if request.auth != null;
    }
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}`;

async function main() {
  const client = await auth.getClient();
  const token = await client.getAccessToken();
  const projectId = 'guardian-d31cf';

  // Create ruleset
  const rulesetRes = await fetch(`https://firebaserules.googleapis.com/v1/projects/${projectId}/rulesets`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token.token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ source: { files: [{ name: 'firestore.rules', content: rules }] } })
  });
  const ruleset = await rulesetRes.json();
  console.log('Ruleset created:', ruleset.name);

  // Update release
  const releaseRes = await fetch(`https://firebaserules.googleapis.com/v1/projects/${projectId}/releases/cloud.firestore`, {
    method: 'PATCH',
    headers: { 'Authorization': `Bearer ${token.token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ ruleset_name: ruleset.name })
  });
  console.log('Release status:', releaseRes.status);
  const release = await releaseRes.json();
  console.log('Release response:', JSON.stringify(release, null, 2));
}

main().catch(console.error);
