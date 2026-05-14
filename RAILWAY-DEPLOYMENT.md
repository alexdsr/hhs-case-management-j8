# Railway Deployment Guide — HHS Case Management (Java 8)

## Prerequisites

- GitHub account
- Railway account (free at railway.app) — sign in with GitHub

---

## Step 1 — Push to GitHub

From the project root (where `pom.xml` lives):

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/hhs-case-management-j8.git
git push -u origin main
```

---

## Step 2 — Create a Railway Project

1. Go to **https://railway.app** and sign in
2. Click **New Project**
3. Choose **Deploy from GitHub repo**
4. Select **hhs-case-management-j8** from the list
5. Railway detects the `Dockerfile` automatically — click **Deploy Now**

---

## Step 3 — Configure the Port

1. In your Railway project, click the service tile
2. Go to **Variables** tab
3. Add this variable:

| Name | Value |
|------|-------|
| `PORT` | `8080` |

---

## Step 4 — Generate a Public URL

1. Go to the **Settings** tab of your service
2. Under **Networking**, click **Generate Domain**
3. Railway gives you a URL like `hhs-case-management-j8.up.railway.app`

---

## Step 5 — Wait for Deployment

The first deploy takes **5–8 minutes**:
- Pull Maven + Java 8 build image
- Download Maven dependencies
- Pull WildFly 18 image
- Build the WAR
- Start WildFly (25–40 seconds)

---

## Step 6 — Access the App

```
https://YOUR-APP.up.railway.app/hhs-case-management-j8/
```

### Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Administrator | admin@hhs.gov | Admin1234! |
| Caseworker | caseworker@hhs.gov | Case1234! |

---

## Important Notes

### Data Resets on Restart
H2 database is stored inside the container at `/opt/jboss/hhsdb_j8/hhsdb_j8.mv.db`.
Container restarts restore seed data. Expected behavior for a demo.

### Java 8 Image
The runtime image is `jboss/wildfly:18.0.1.Final` which includes Java 8.
This is pulled from Docker Hub.

### Free Tier Limits
WildFly 18 uses ~400–512MB RAM. Monitor usage in Railway's Metrics tab.

---

## Troubleshooting

**Health check fails**
- WildFly 18 needs 20–35 seconds to fully start — check Logs tab
- Look for `INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0025` 
  which confirms WildFly is started

**Datasource not found**
- Look for `HHSDataSource` in the logs after the CLI script runs
- If missing, the CLI script may have failed — check for errors around the
  25-second mark in the logs

**OutOfMemoryError**
- Add to Dockerfile CMD: `-Xms256m -Xmx512m` after standalone.sh

---

## Re-deploying After Code Changes

```bash
git add .
git commit -m "Your changes"
git push
```

Railway auto-deploys on every push to main.
