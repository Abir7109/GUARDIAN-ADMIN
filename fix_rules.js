import admin from 'firebase-admin';
import sa from './service-account.json' with { type: 'json' };

admin.initializeApp({ credential: admin.credential.cert(sa), projectId: 'guardian-d31cf' });

const rulesText = `rules_version = '2';
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
  const sr = admin.securityRules();
  const release = await sr.releaseFirestoreRulesetFromSource(rulesText);
  console.log('Rules updated:', release.name);
  console.log('Ruleset:', release.rulesetName);
}

main().catch(console.error);
