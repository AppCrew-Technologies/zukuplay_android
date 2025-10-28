#!/usr/bin/env node

/**
 * NextPlayer cPanel Deployment Builder
 * Automates the preparation of files for cPanel hosting
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('🚀 NextPlayer cPanel Deployment Builder\n');

// Configuration
const ADMIN_PANEL_PATH = '../admin-panel';
const PUBLIC_HTML_PATH = './public_html';
const FIREBASE_KEY_PATH = '../app';

/**
 * Copy directory recursively
 */
function copyDir(src, dest) {
    if (!fs.existsSync(dest)) {
        fs.mkdirSync(dest, { recursive: true });
    }
    
    const files = fs.readdirSync(src);
    
    for (const file of files) {
        const srcPath = path.join(src, file);
        const destPath = path.join(dest, file);
        
        if (fs.statSync(srcPath).isDirectory()) {
            copyDir(srcPath, destPath);
        } else {
            fs.copyFileSync(srcPath, destPath);
        }
    }
}

/**
 * Build React app
 */
function buildReactApp() {
    console.log('📦 Building React app...');
    
    try {
        // Check if admin-panel exists
        if (!fs.existsSync(ADMIN_PANEL_PATH)) {
            console.error('❌ admin-panel folder not found!');
            console.log('Make sure this script is run from cpanel-deployment folder');
            process.exit(1);
        }
        
        // Build the React app
        console.log('Installing dependencies...');
        execSync('npm install', { cwd: ADMIN_PANEL_PATH, stdio: 'inherit' });
        
        console.log('Building React app...');
        execSync('npm run build', { cwd: ADMIN_PANEL_PATH, stdio: 'inherit' });
        
        // Copy build files to public_html
        const buildPath = path.join(ADMIN_PANEL_PATH, 'build');
        if (fs.existsSync(buildPath)) {
            console.log('📁 Copying build files to public_html...');
            
            // Remove existing public_html contents (except placeholder)
            if (fs.existsSync(PUBLIC_HTML_PATH)) {
                fs.rmSync(PUBLIC_HTML_PATH, { recursive: true, force: true });
            }
            
            copyDir(buildPath, PUBLIC_HTML_PATH);
            console.log('✅ React build copied successfully!');
        } else {
            console.error('❌ Build folder not found! React build may have failed.');
            process.exit(1);
        }
        
    } catch (error) {
        console.error('❌ Error building React app:', error.message);
        process.exit(1);
    }
}

/**
 * Find and copy Firebase admin key
 */
function setupFirebaseKey() {
    console.log('🔑 Setting up Firebase admin key...');
    
    try {
        // Look for Firebase key in various locations
        const possiblePaths = [
            path.join(FIREBASE_KEY_PATH, 'zukuplayer-b7a68-firebase-adminsdk-fbsvc-caabf30cbb.json'),
            path.join('../', 'firebase-admin-key.json'),
            path.join('./', 'firebase-admin-key.json')
        ];
        
        let keyFound = false;
        
        for (const keyPath of possiblePaths) {
            if (fs.existsSync(keyPath)) {
                console.log(`📥 Found Firebase key at: ${keyPath}`);
                fs.copyFileSync(keyPath, './firebase-admin-key.json');
                console.log('✅ Firebase key copied successfully!');
                keyFound = true;
                break;
            }
        }
        
        if (!keyFound) {
            console.log('⚠️  Firebase admin key not found automatically.');
            console.log('Please manually copy your Firebase admin key to:');
            console.log('   ./firebase-admin-key.json');
            console.log('');
            console.log('Or download it from Firebase Console:');
            console.log('   Project Settings → Service Accounts → Generate new private key');
        }
        
    } catch (error) {
        console.error('❌ Error setting up Firebase key:', error.message);
    }
}

/**
 * Update API endpoints in React build for cPanel
 */
function updateApiEndpoints() {
    console.log('🔧 Updating API endpoints for cPanel...');
    
    try {
        const staticJsPath = path.join(PUBLIC_HTML_PATH, 'static', 'js');
        
        if (fs.existsSync(staticJsPath)) {
            const jsFiles = fs.readdirSync(staticJsPath).filter(file => file.endsWith('.js'));
            
            for (const file of jsFiles) {
                const filePath = path.join(staticJsPath, file);
                let content = fs.readFileSync(filePath, 'utf8');
                
                // Replace localhost API calls with relative paths
                content = content.replace(/http:\/\/localhost:3000/g, '');
                content = content.replace(/\/send-notification/g, '/api/send-notification');
                content = content.replace(/\/health/g, '/api/health');
                
                fs.writeFileSync(filePath, content);
            }
            
            console.log('✅ API endpoints updated for cPanel!');
        }
        
    } catch (error) {
        console.error('❌ Error updating API endpoints:', error.message);
    }
}

/**
 * Validate deployment structure
 */
function validateDeployment() {
    console.log('🔍 Validating deployment structure...');
    
    const requiredFiles = [
        'server.js',
        'package.json',
        '.htaccess',
        'public_html/index.html'
    ];
    
    const optionalFiles = [
        'firebase-admin-key.json'
    ];
    
    let allGood = true;
    
    for (const file of requiredFiles) {
        if (fs.existsSync(file)) {
            console.log(`✅ ${file}`);
        } else {
            console.log(`❌ ${file} - MISSING!`);
            allGood = false;
        }
    }
    
    for (const file of optionalFiles) {
        if (fs.existsSync(file)) {
            console.log(`✅ ${file}`);
        } else {
            console.log(`⚠️  ${file} - Not found (manual setup required)`);
        }
    }
    
    if (allGood) {
        console.log('\n🎉 Deployment structure looks good!');
        console.log('\n📋 Next steps:');
        console.log('1. Upload all files to your cPanel');
        console.log('2. Configure Node.js app in cPanel');
        console.log('3. Set up domain/subdomain');
        console.log('4. Test the deployment');
        console.log('\nSee README-CPANEL-DEPLOYMENT.md for detailed instructions.');
    } else {
        console.log('\n❌ Some required files are missing!');
        process.exit(1);
    }
}

/**
 * Main execution
 */
function main() {
    try {
        console.log('Starting cPanel deployment preparation...\n');
        
        // Step 1: Build React app
        buildReactApp();
        
        // Step 2: Setup Firebase key
        setupFirebaseKey();
        
        // Step 3: Update API endpoints
        updateApiEndpoints();
        
        // Step 4: Validate everything
        validateDeployment();
        
        console.log('\n🚀 cPanel deployment preparation complete!');
        
    } catch (error) {
        console.error('❌ Deployment preparation failed:', error.message);
        process.exit(1);
    }
}

// Run if called directly
if (require.main === module) {
    main();
}

module.exports = { main, buildReactApp, setupFirebaseKey, updateApiEndpoints, validateDeployment }; 