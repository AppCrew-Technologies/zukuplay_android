const express = require('express');
const admin = require('firebase-admin');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Serve React static files from public_html directory
app.use(express.static(path.join(__dirname, 'public_html')));

// Initialize Firebase Admin SDK - Updated for cPanel deployment
const serviceAccountPath = path.join(__dirname, 'firebase-admin-key.json');

try {
  const serviceAccount = require(serviceAccountPath);
  
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    projectId: serviceAccount.project_id
  });
  
  console.log('✅ Firebase Admin SDK initialized successfully');
} catch (error) {
  console.error('❌ Error initializing Firebase Admin SDK:', error.message);
  console.error('Make sure firebase-admin-key.json is in the root directory');
  process.exit(1);
}

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    message: 'FCM Notification Server is running on cPanel',
    timestamp: new Date().toISOString(),
    environment: 'production'
  });
});

// Send notification endpoint
app.post('/api/send-notification', async (req, res) => {
  try {
    console.log('📨 Received notification request:', {
      body: JSON.stringify(req.body, null, 2),
      headers: req.headers,
      timestamp: new Date().toISOString()
    });

    const { title, body, topic = 'all', priority = 'normal', data = {}, cta, imageUrl, backgroundColor, textColor, buttons = [] } = req.body;

    // Validate required fields
    if (!title || !body) {
      console.error('❌ Validation failed: Missing title or body');
      return res.status(400).json({
        success: false,
        error: 'Missing required fields: title and body are required'
      });
    }

    // Validate priority
    if (priority && !['normal', 'high'].includes(priority)) {
      return res.status(400).json({
        success: false,
        error: 'Priority must be either "normal" or "high"'
      });
    }

    // Construct the message payload using working format
    const message = {
      topic: topic,
      notification: {
        title: title,
        body: body
      },
      android: {
        priority: priority === 'high' ? 'high' : 'normal',
        notification: {
          title: title,
          body: body,
          channelId: 'nextplayer_notifications'
        },
        data: {}
      },
      apns: {
        payload: {
          aps: {
            alert: {
              title: title,
              body: body
            },
            sound: 'default',
            badge: 1
          }
        },
        headers: {
          'apns-priority': priority === 'high' ? '10' : '5'
        }
      }
    };

    // Add rich content data for in-app popup
    const messageData = { ...data };
    
    // Always mark as rich popup notification
    messageData.show_rich_popup = 'true';
    messageData.from_notification = 'true';
    
    // Always include notification title and body for popup display
    messageData.notification_title = title;
    messageData.notification_body = body;
    
    // Add rich content data
    if (imageUrl) {
      messageData.image_url = imageUrl;
    }
    
    if (backgroundColor) {
      messageData.background_color = backgroundColor;
    }
    
    if (textColor) {
      messageData.text_color = textColor;
    }
    
    // Add main CTA if provided
    if (cta && cta.enabled && cta.text && cta.link) {
      messageData.cta_text = cta.text;
      messageData.cta_link = cta.link;
      console.log('🔗 Adding main CTA to rich popup:', { text: cta.text, link: cta.link });
    }
    
    // Add additional buttons if provided
    if (buttons && buttons.length > 0) {
      messageData.buttons_count = buttons.length.toString();
      buttons.forEach((button, index) => {
        messageData[`button_${index}_text`] = button.text;
        messageData[`button_${index}_link`] = button.link;
        messageData[`button_${index}_style`] = button.style || 'PRIMARY';
      });
      console.log('🎨 Adding additional buttons to rich popup:', buttons.length);
    }
    
    // Copy all data to Android-specific section
    message.android.data = { ...messageData };
    
    console.log('🎨 Rich notification prepared for in-app popup display');

    // Add custom data if provided
    if (Object.keys(messageData).length > 0) {
      message.data = messageData;
    }

    console.log('📤 Sending notification:', {
      title,
      body,
      topic,
      priority,
      cta: cta && cta.enabled ? { text: cta.text, link: cta.link } : 'none',
      timestamp: new Date().toISOString()
    });

    // Send the message
    const response = await admin.messaging().send(message);
    
    console.log('✅ Notification sent successfully:', response);

    res.status(200).json({
      success: true,
      message: 'Notification sent successfully',
      messageId: response,
      sentTo: topic,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    console.error('❌ Error sending notification:', {
      message: error.message,
      stack: error.stack,
      code: error.code,
      timestamp: new Date().toISOString()
    });
    
    // Send detailed error response
    res.status(500).json({
      success: false,
      error: 'Failed to send notification',
      details: error.message,
      errorCode: error.code,
      timestamp: new Date().toISOString()
    });
  }
});

// Send notification to multiple topics
app.post('/api/send-notification-multicast', async (req, res) => {
  try {
    const { title, body, topics = ['all'], priority = 'normal', data = {} } = req.body;

    // Validate required fields
    if (!title || !body) {
      return res.status(400).json({
        success: false,
        error: 'Missing required fields: title and body are required'
      });
    }

    if (!Array.isArray(topics) || topics.length === 0) {
      return res.status(400).json({
        success: false,
        error: 'Topics must be a non-empty array'
      });
    }

    // Send to multiple topics
    const promises = topics.map(topic => {
      const message = {
        notification: {
          title: title,
          body: body
        },
        topic: topic,
        android: {
          priority: priority,
          notification: {
            title: title,
            body: body,
            sound: 'default',
            channelId: 'default'
          }
        },
        apns: {
          payload: {
            aps: {
              alert: {
                title: title,
                body: body
              },
              sound: 'default',
              badge: 1
            }
          },
          headers: {
            'apns-priority': priority === 'high' ? '10' : '5'
          }
        }
      };

      if (Object.keys(data).length > 0) {
        message.data = data;
      }

      return admin.messaging().send(message);
    });

    console.log('📤 Sending multicast notification:', {
      title,
      body,
      topics,
      priority,
      timestamp: new Date().toISOString()
    });

    const responses = await Promise.allSettled(promises);
    
    const results = responses.map((response, index) => ({
      topic: topics[index],
      success: response.status === 'fulfilled',
      messageId: response.status === 'fulfilled' ? response.value : null,
      error: response.status === 'rejected' ? response.reason.message : null
    }));

    const successCount = results.filter(r => r.success).length;
    
    console.log('✅ Multicast notification completed:', {
      total: topics.length,
      success: successCount,
      failed: topics.length - successCount
    });

    res.status(200).json({
      success: true,
      message: `Notification sent to ${successCount}/${topics.length} topics`,
      results: results,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    console.error('❌ Error sending multicast notification:', error);
    
    res.status(500).json({
      success: false,
      error: 'Failed to send multicast notification',
      details: error.message,
      timestamp: new Date().toISOString()
    });
  }
});

// Get topic subscription count
app.get('/api/topic/:topicName/info', async (req, res) => {
  try {
    const { topicName } = req.params;
    
    res.status(200).json({
      topic: topicName,
      message: 'Topic information endpoint - subscription count not available via Admin SDK',
      timestamp: new Date().toISOString()
    });
    
  } catch (error) {
    console.error('❌ Error getting topic info:', error);
    
    res.status(500).json({
      success: false,
      error: 'Failed to get topic information',
      details: error.message
    });
  }
});

// Catch-all handler for React Router (must be after API routes)
app.get('*', (req, res) => {
  // Only serve React app for non-API routes
  if (!req.path.startsWith('/api/')) {
    res.sendFile(path.join(__dirname, 'public_html', 'index.html'));
  } else {
    res.status(404).json({
      success: false,
      error: 'API endpoint not found',
      availableEndpoints: [
        'GET /api/health',
        'POST /api/send-notification',
        'POST /api/send-notification-multicast',
        'GET /api/topic/:topicName/info'
      ]
    });
  }
});

// Error handling middleware
app.use((error, req, res, next) => {
  console.error('❌ Unhandled error:', error);
  res.status(500).json({
    success: false,
    error: 'Internal server error',
    message: error.message
  });
});

// Start server
const server = app.listen(PORT, () => {
  console.log(`🚀 FCM Notification Server is running on port ${PORT}`);
  console.log(`📱 Health check: http://localhost:${PORT}/api/health`);
  console.log(`📤 Send notification: POST http://localhost:${PORT}/api/send-notification`);
  console.log(`🌐 Frontend served from: http://localhost:${PORT}/`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM received, shutting down gracefully');
  server.close(() => {
    console.log('Process terminated');
  });
});

module.exports = app; 