const sa = require('D:\\Guardian ANTI THIEF\\ADMIN PANEL\\securephone-admin\\service-account.json');
const {JWT} = require('google-auth-library');
const client = new JWT({
  email: sa.client_email,
  key: sa.private_key,
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

client.authorize().then(tokens => {
  const token = tokens.access_token;
  const projectId = 'guardian-d31cf';
  const https = require('https');

  // Create new ruleset
  const data = JSON.stringify({
    source: { files: [{ name: 'firestore.rules', content: rules }] }
  });

  const req = https.request({
    hostname: 'firebaserules.googleapis.com',
    path: '/v1/projects/' + projectId + '/rulesets',
    method: 'POST',
    headers: {
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(data)
    }
  }, (res) => {
    let body = '';
    res.on('data', chunk => body += chunk);
    res.on('end', () => {
      console.log('Create Ruleset Status:', res.statusCode);
      if (res.statusCode === 200) {
        const rs = JSON.parse(body);
        console.log('Ruleset created:', rs.name);

        // First try to GET the release to see its format
        const releasePath = 'projects/' + projectId + '/releases/cloud.firestore';
        const reqGet = https.request({
          hostname: 'firebaserules.googleapis.com',
          path: '/v1/' + releasePath,
          method: 'GET',
          headers: { 'Authorization': 'Bearer ' + token }
        }, (resGet) => {
          let bodyGet = '';
          resGet.on('data', chunk => bodyGet += chunk);
          resGet.on('end', () => {
            console.log('GET Release Status:', resGet.statusCode);
            console.log('GET Release Response:', bodyGet);
            
            if (resGet.statusCode === 200 || resGet.statusCode === 404) {
              // Try creating/replacing release via POST or PUT
              const releaseBody = JSON.stringify({
                name: releasePath,
                rulesetName: rs.name
              });
              const req2 = https.request({
                hostname: 'firebaserules.googleapis.com',
                path: '/v1/' + releasePath,
                method: 'PUT',
                headers: {
                  'Authorization': 'Bearer ' + token,
                  'Content-Type': 'application/json',
                  'Content-Length': Buffer.byteLength(releaseBody)
                }
              }, (res2) => {
                let body2 = '';
                res2.on('data', chunk => body2 += chunk);
                res2.on('end', () => {
                  console.log('Release PUT Status:', res2.statusCode);
                  console.log('Release PUT Response:', body2);
                });
              });
              req2.write(releaseBody);
              req2.end();
            }
          });
        });
        reqGet.end();
      } else {
        console.log('Error response:', body);
      }
    });
  });
  req.write(data);
  req.end();
}).catch(err => console.error('Auth error:', err));
