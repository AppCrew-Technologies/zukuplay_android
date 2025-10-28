const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Firebase function that triggers when a new broadcast message 
 * is added to the 'broadcasts' collection.
 * It sends FCM notifications to all registered devices.
 */
exports.sendBroadcastNotification = functions.firestore
  .document('broadcasts/{broadcastId}')
  .onCreate(async (snapshot, context) => {
    const broadcastData = snapshot.data();
    
    // Skip if it's a scheduled message for later
    if (broadcastData.schedule === 'later') {
      console.log('Broadcast scheduled for later, skipping immediate send');
      return null;
    }
    
    // Get all device tokens from the device_tokens collection
    const tokensSnapshot = await admin.firestore()
      .collection('device_tokens')
      .get();
    
    const tokens = [];
    tokensSnapshot.forEach(doc => {
      tokens.push(doc.data().token);
    });
    
    if (tokens.length === 0) {
      console.log('No device tokens found to send notifications');
      return null;
    }
    
    console.log(`Found ${tokens.length} device tokens to send notifications to`);
    
    // Create notification payload
    const notification = {
      title: broadcastData.title,
      body: broadcastData.message,
    };
    
    // Create data payload
    const data = {
      type: broadcastData.type || 'general',
      broadcast_id: context.params.broadcastId,
      created_at: broadcastData.createdAt ? broadcastData.createdAt.toString() : Date.now().toString()
    };
    
    // Add CTA if present
    if (broadcastData.cta) {
      data.cta_text = broadcastData.cta.text;
      data.cta_link = broadcastData.cta.link;
    }
    
    // Send message to all devices in batches of 500 (FCM limit)
    const batchSize = 500;
    const batches = Math.ceil(tokens.length / batchSize);
    
    for (let i = 0; i < batches; i++) {
      const start = i * batchSize;
      const end = Math.min(tokens.length, (i + 1) * batchSize);
      const batchTokens = tokens.slice(start, end);
      
      const message = {
        notification: notification,
        data: data,
        tokens: batchTokens,
        android: {
          priority: broadcastData.priority === 'high' ? 'high' : 'normal',
          notification: {
            clickAction: 'OPEN_NOTIFICATION_ACTIVITY',
            channelId: 'next_player_notifications'
          }
        }
      };
      
      try {
        const response = await admin.messaging().sendMulticast(message);
        console.log(`Batch ${i+1}/${batches}: ${response.successCount} messages sent successfully`);
        
        // Update the broadcast document with delivery stats
        await admin.firestore()
          .collection('broadcasts')
          .doc(context.params.broadcastId)
          .update({
            deliveryCount: admin.firestore.FieldValue.increment(response.successCount),
            failureCount: admin.firestore.FieldValue.increment(response.failureCount),
            status: 'sent',
            sentAt: admin.firestore.FieldValue.serverTimestamp()
          });
        
        // Handle failed tokens
        if (response.failureCount > 0) {
          const failedTokens = [];
          response.responses.forEach((resp, idx) => {
            if (!resp.success) {
              failedTokens.push(batchTokens[idx]);
              console.log('Failed to send to token:', batchTokens[idx], resp.error);
            }
          });
          
          // Remove invalid tokens
          const invalidTokenErrors = ['messaging/invalid-registration-token', 'messaging/registration-token-not-registered'];
          for (const token of failedTokens) {
            try {
              // Get the response for this token
              const tokenIndex = batchTokens.indexOf(token);
              const errorCode = response.responses[tokenIndex].error ? response.responses[tokenIndex].error.code : null;
              
              // Only delete tokens that are invalid, not tokens that failed for other reasons
              if (invalidTokenErrors.includes(errorCode)) {
                console.log(`Deleting invalid token: ${token}`);
                await admin.firestore().collection('device_tokens').doc(token).delete();
              }
            } catch (error) {
              console.error('Error handling failed token:', error);
            }
          }
        }
      } catch (error) {
        console.error('Error sending multicast messages:', error);
      }
    }
    
    return null;
  });

/**
 * Firebase function to process scheduled broadcasts that should be sent.
 * This function runs on a schedule (every 15 minutes) and checks for broadcasts 
 * that are scheduled to be sent now.
 */
exports.processScheduledBroadcasts = functions.pubsub
  .schedule('every 15 minutes')
  .onRun(async (context) => {
    const now = admin.firestore.Timestamp.now();
    
    try {
      // Get all broadcasts scheduled for "later" that should be sent by now
      const scheduledBroadcastsSnapshot = await admin.firestore()
        .collection('broadcasts')
        .where('schedule', '==', 'later')
        .where('status', '==', 'scheduled')
        .where('scheduledDate', '<=', now)
        .get();
      
      console.log(`Found ${scheduledBroadcastsSnapshot.size} scheduled broadcasts to process`);
      
      if (scheduledBroadcastsSnapshot.empty) {
        return null;
      }
      
      // Process each scheduled broadcast
      const promises = scheduledBroadcastsSnapshot.docs.map(async (doc) => {
        const broadcastId = doc.id;
        const broadcastData = doc.data();
        
        // First update the status to 'processing' to prevent duplicate sends
        await admin.firestore()
          .collection('broadcasts')
          .doc(broadcastId)
          .update({
            status: 'processing',
            processedAt: admin.firestore.FieldValue.serverTimestamp()
          });
        
        // Get device tokens
        const tokensSnapshot = await admin.firestore()
          .collection('device_tokens')
          .get();
        
        const tokens = tokensSnapshot.docs.map(doc => doc.data().token);
        
        if (tokens.length === 0) {
          console.log(`No tokens to send broadcast ${broadcastId}`);
          return admin.firestore()
            .collection('broadcasts')
            .doc(broadcastId)
            .update({
              status: 'failed',
              failureReason: 'No active device tokens found',
              sentAt: admin.firestore.FieldValue.serverTimestamp()
            });
        }
        
        // Send notification using the same code as the onCreate handler
        const notification = {
          title: broadcastData.title,
          body: broadcastData.message,
        };
        
        const data = {
          type: broadcastData.type || 'general',
          broadcast_id: broadcastId,
          created_at: broadcastData.createdAt ? broadcastData.createdAt.toString() : Date.now().toString()
        };
        
        if (broadcastData.cta) {
          data.cta_text = broadcastData.cta.text;
          data.cta_link = broadcastData.cta.link;
        }
        
        // Send in batches like the onCreate handler
        const batchSize = 500;
        const batches = Math.ceil(tokens.length / batchSize);
        let totalSuccess = 0;
        let totalFailure = 0;
        
        for (let i = 0; i < batches; i++) {
          const start = i * batchSize;
          const end = Math.min(tokens.length, (i + 1) * batchSize);
          const batchTokens = tokens.slice(start, end);
          
          const message = {
            notification: notification,
            data: data,
            tokens: batchTokens,
            android: {
              priority: broadcastData.priority === 'high' ? 'high' : 'normal',
              notification: {
                clickAction: 'OPEN_NOTIFICATION_ACTIVITY',
                channelId: 'next_player_notifications'
              }
            }
          };
          
          try {
            const response = await admin.messaging().sendMulticast(message);
            console.log(`Scheduled broadcast ${broadcastId} batch ${i+1}/${batches}: ${response.successCount} messages sent successfully`);
            
            totalSuccess += response.successCount;
            totalFailure += response.failureCount;
            
            // Handle failed tokens (same as in onCreate handler)
            if (response.failureCount > 0) {
              const failedTokens = [];
              response.responses.forEach((resp, idx) => {
                if (!resp.success) {
                  failedTokens.push(batchTokens[idx]);
                  console.log('Failed to send to token:', batchTokens[idx], resp.error);
                }
              });
              
              // Remove invalid tokens
              const invalidTokenErrors = ['messaging/invalid-registration-token', 'messaging/registration-token-not-registered'];
              for (const token of failedTokens) {
                try {
                  const tokenIndex = batchTokens.indexOf(token);
                  const errorCode = response.responses[tokenIndex].error ? response.responses[tokenIndex].error.code : null;
                  
                  if (invalidTokenErrors.includes(errorCode)) {
                    console.log(`Deleting invalid token: ${token}`);
                    await admin.firestore().collection('device_tokens').doc(token).delete();
                  }
                } catch (error) {
                  console.error('Error handling failed token:', error);
                }
              }
            }
          } catch (error) {
            console.error(`Error sending scheduled broadcast ${broadcastId}:`, error);
            totalFailure += batchTokens.length;
          }
        }
        
        // Update the broadcast document with final status
        return admin.firestore()
          .collection('broadcasts')
          .doc(broadcastId)
          .update({
            deliveryCount: totalSuccess,
            failureCount: totalFailure,
            status: totalSuccess > 0 ? 'sent' : 'failed',
            failureReason: totalSuccess > 0 ? null : 'Failed to send to any devices',
            sentAt: admin.firestore.FieldValue.serverTimestamp()
          });
      });
      
      await Promise.all(promises);
      console.log('Completed processing scheduled broadcasts');
      return null;
    } catch (error) {
      console.error('Error processing scheduled broadcasts:', error);
      return null;
    }
  }); 