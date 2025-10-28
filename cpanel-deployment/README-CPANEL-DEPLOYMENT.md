# 🚀 NextPlayer Admin Panel - cPanel Deployment Guide

## 📁 Folder Structure Overview

```
cpanel-deployment/
├── server.js                          # Node.js backend (optimized for cPanel)
├── package.json                       # Backend dependencies only
├── firebase-admin-key.json.example    # Firebase credentials template
├── .htaccess                          # Apache configuration
├── public_html/                       # React app goes here
│   └── index.html                     # Placeholder (replace with build)
├── backend/                           # Development folder (not needed on cPanel)
├── frontend/                          # Development folder (not needed on cPanel)
└── README-CPANEL-DEPLOYMENT.md       # This file
```

## 🎯 What to Upload to cPanel

### ✅ Upload These Files:
- `server.js`
- `package.json`
- `firebase-admin-key.json` (your actual Firebase key)
- `.htaccess`
- `public_html/` folder with React build files

### ❌ Don't Upload:
- `backend/` folder
- `frontend/` folder
- `firebase-admin-key.json.example`
- Any `node_modules/` folders
- Development files

## 📋 Step-by-Step Deployment Instructions

### Phase 1: Prepare React Build

1. **Navigate to your admin-panel folder:**
   ```bash
   cd admin-panel
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Build React app:**
   ```bash
   npm run build
   ```

4. **Copy build files:**
   - Copy everything from `admin-panel/build/` 
   - Paste to `cpanel-deployment/public_html/`
   - Replace the placeholder `index.html`

### Phase 2: Prepare Firebase Credentials

1. **Get Firebase Admin SDK Key:**
   - Go to Firebase Console → Project Settings
   - Service Accounts → Generate new private key
   - Download the JSON file

2. **Rename and place:**
   - Rename downloaded file to `firebase-admin-key.json`
   - Place in `cpanel-deployment/` root

### Phase 3: Upload to cPanel

1. **Access cPanel File Manager**
2. **Navigate to your domain folder** (usually `public_html/yourapp/`)
3. **Upload these files:**
   ```
   server.js
   package.json
   firebase-admin-key.json
   .htaccess
   public_html/ (entire folder)
   ```

### Phase 4: Configure Node.js in cPanel

1. **Find "Node.js Selector" or "Node.js App" in cPanel**
2. **Create New Application:**
   - **Node.js Version:** 18.x or latest
   - **Application Mode:** Production
   - **Application Root:** `/public_html/yourapp` (your app folder)
   - **Application URL:** `yourapp` or subdomain
   - **Application Startup File:** `server.js`

3. **Set Environment Variables:**
   - `NODE_ENV=production`
   - `PORT=3000` (cPanel will override this)

### Phase 5: Install Dependencies

1. **Option A: cPanel Terminal (if available)**
   ```bash
   cd /public_html/yourapp
   npm install
   ```

2. **Option B: cPanel Node.js Interface**
   - Go to Node.js Selector
   - Click on your app
   - Click "NPM Install"

### Phase 6: Start the Application

1. **In Node.js Selector:**
   - Click "Restart" or "Start"
   - Status should show "Running"

2. **Test the deployment:**
   - Visit: `https://yourdomain.com/yourapp/`
   - Check API: `https://yourdomain.com/yourapp/api/health`

## 🔧 Configuration Details

### API Endpoints (with /api prefix):
- `GET /api/health` - Server health check
- `POST /api/send-notification` - Send FCM notification
- `POST /api/send-notification-multicast` - Send to multiple topics
- `GET /api/topic/:topicName/info` - Topic information

### Frontend Routes:
- `/` - Main admin dashboard
- `/login` - Admin login
- All React routes work with browser refresh

## 🔒 Security Features

- Firebase key protected by .htaccess
- Security headers enabled
- File access restrictions
- HTTPS enforcement (configure in cPanel)

## 🚨 Troubleshooting

### Common Issues:

1. **"Firebase key not found"**
   - Ensure `firebase-admin-key.json` is in root directory
   - Check file permissions (644)

2. **"Cannot connect to Firebase"**
   - Verify Firebase project ID in key file
   - Check cPanel firewall settings

3. **React app shows blank page**
   - Ensure build files are in `public_html/`
   - Check .htaccess rewrite rules

4. **API routes return 404**
   - Verify Node.js app is running
   - Check server.js is the startup file

### Log Files:
- cPanel Error Logs: `/logs/error_log`
- Node.js Logs: Available in cPanel Node.js interface

## 📊 Monitoring

### Check if Server is Running:
```bash
# Health check
curl https://yourdomain.com/yourapp/api/health

# Should return:
{
  "status": "healthy",
  "message": "FCM Notification Server is running on cPanel",
  "timestamp": "2024-01-01T00:00:00.000Z",
  "environment": "production"
}
```

### Performance Monitoring:
- Use cPanel resource usage stats
- Monitor Node.js app memory usage
- Check API response times

## 🔄 Updates

### To Update Your App:
1. Rebuild React app locally
2. Replace `public_html/` contents
3. Update `server.js` if needed
4. Restart Node.js app in cPanel

### To Update Dependencies:
1. Update `package.json`
2. Upload to cPanel
3. Run `npm install` via cPanel terminal
4. Restart application

## 📞 Support

If you encounter issues:
1. Check cPanel error logs
2. Verify Node.js version compatibility
3. Ensure all files are uploaded correctly
4. Contact your hosting provider for Node.js support

---

**✅ Deployment Complete!**

Your NextPlayer Admin Panel should now be running 24/7 on cPanel hosting.

Frontend: `https://yourdomain.com/yourapp/`
Backend API: `https://yourdomain.com/yourapp/api/` 