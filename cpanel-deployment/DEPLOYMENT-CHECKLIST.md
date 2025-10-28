# ✅ cPanel Deployment Checklist

## 📋 Pre-Deployment Checklist

### Local Preparation:
- [ ] React app builds successfully (`npm run build` in admin-panel)
- [ ] Firebase admin key downloaded from Firebase Console
- [ ] All dependencies installed locally
- [ ] Server.js tested locally

### Files to Upload:
- [ ] `server.js` (optimized for cPanel)
- [ ] `package.json` (backend dependencies only)
- [ ] `firebase-admin-key.json` (your actual Firebase key)
- [ ] `.htaccess` (Apache configuration)
- [ ] `public_html/` folder (complete React build)

### cPanel Configuration:
- [ ] Node.js Selector configured
- [ ] Application startup file set to `server.js`
- [ ] Node.js version 18+ selected
- [ ] Production mode enabled
- [ ] Domain/subdomain configured

## 🚀 Deployment Steps

### 1. Build React App
```bash
cd admin-panel
npm install
npm run build
```

### 2. Prepare Firebase Key
- Download from Firebase Console → Project Settings → Service Accounts
- Rename to `firebase-admin-key.json`
- Place in deployment root

### 3. Upload to cPanel
Upload these files to your cPanel app folder:
```
server.js
package.json
firebase-admin-key.json
.htaccess
public_html/ (entire folder with React build)
```

### 4. Configure Node.js App
- Go to cPanel → Node.js Selector
- Create new application
- Set startup file: `server.js`
- Install dependencies
- Start application

### 5. Test Deployment
- [ ] Frontend loads: `https://yourdomain.com/yourapp/`
- [ ] API responds: `https://yourdomain.com/yourapp/api/health`
- [ ] Notifications work: Test FCM endpoint
- [ ] All React routes work

## 🔧 Quick Commands

### Build Everything Automatically:
```bash
cd cpanel-deployment
node build-for-cpanel.js
```

### Test API Health:
```bash
curl https://yourdomain.com/yourapp/api/health
```

### Expected Response:
```json
{
  "status": "healthy",
  "message": "FCM Notification Server is running on cPanel",
  "timestamp": "2024-01-01T00:00:00.000Z",
  "environment": "production"
}
```

## 🚨 Troubleshooting

### Server Won't Start:
1. Check Node.js version (18+)
2. Verify `firebase-admin-key.json` exists
3. Check error logs in cPanel
4. Ensure all dependencies installed

### Frontend Shows Blank Page:
1. Verify React build files in `public_html/`
2. Check `.htaccess` rules
3. Clear browser cache
4. Check console for errors

### API Returns 404:
1. Ensure Node.js app is running
2. Check API routes have `/api/` prefix
3. Verify server.js startup file
4. Test with direct URL

## 📞 Support Resources

- **cPanel Docs**: Your hosting provider's documentation
- **Node.js Logs**: Available in cPanel Node.js interface
- **Error Logs**: `/logs/error_log` in cPanel
- **Firebase Console**: For FCM configuration

---

## 🎯 Success Criteria

✅ **Deployment is successful when:**
- Frontend loads without errors
- API health check returns 200 OK
- FCM notifications can be sent
- All React routes work with browser refresh
- Server runs 24/7 without manual intervention

**🚀 Your NextPlayer Admin Panel is now live on cPanel!** 