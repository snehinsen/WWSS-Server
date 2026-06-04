# WebSocket Messaging System - Quick Start Guide

## 🚀 Getting Started

### 1. Build the Project
```bash
cd hp-socials-server
./gradlew build
```

### 2. Run the Application
```bash
./gradlew bootRun
```

The server will start on `http://localhost:8080`

### 3. Test the System

#### Option A: Interactive Demo (Recommended)
Open your browser and go to:
```
http://localhost:8080/messaging-demo.html
```

This provides a beautiful interactive interface to test all WebSocket features.

#### Option B: JavaScript Console
Open browser developer console and use:
```javascript
const client = new MessageThreadWebSocketClient();
await client.connect();
```

## 📱 Key WebSocket Features

### Real-Time Message Sending
```javascript
// After connecting and subscribing to a thread:
client.sendMessage(threadId, "Hello World!", []);
```

### Typing Indicators
```javascript
// User is typing
client.sendTypingIndicator(threadId, true);

// User stopped typing
client.sendTypingIndicator(threadId, false);
```

### Thread Management
```javascript
// Join a thread
client.joinThread(threadId);

// Leave a thread
client.leaveThread(threadId);

// Get message history
const history = await client.getMessageHistory(threadId);
```

### Message Operations
```javascript
// Edit a message
await client.editMessage(messageId, "Updated text");

// Delete a message
await client.deleteMessage(messageId);
```

## 🔌 Connection Details

- **WebSocket URL**: `ws://localhost:8080/ws`
- **Protocol**: STOMP
- **Authentication**: Uses existing Spring Security session

## 📊 API Endpoints

### WebSocket (STOMP)
| Destination | Purpose |
|------------|---------|
| `/app/sendMessage` | Send a new message |
| `/app/typing` | Send typing indicator |
| `/app/joinThread` | Notify joining thread |
| `/app/leaveThread` | Notify leaving thread |
| `/topic/thread/{id}` | Subscribe for updates |
| `/user/queue/errors` | Receive error messages |

### REST (HTTP)
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/messages/history` | POST | Get message history |
| `/api/messages/delete` | POST | Delete a message |
| `/api/messages/edit` | POST | Edit a message |
| `/api/dms` | GET | Get all threads |
| `/api/dms/{threadId}/info` | GET | Get thread info |

## 💾 Database Operations

All messages are automatically persisted to PostgreSQL:

```sql
SELECT * FROM dm_messages WHERE thread_id = 1 ORDER BY timestamp DESC;
SELECT * FROM chatss WHERE id = 1;
```

## 🧪 Test Scenarios

### Scenario 1: Simple Messaging
1. Open demo page in two browser windows
2. Thread 1, Window A sends message
3. Message appears in Window B in real-time
4. Both windows connected to same thread

### Scenario 2: Typing Indicators
1. Start typing in Window A
2. Observe "typing" indicator in Window B
3. Stop typing
4. Indicator disappears

### Scenario 3: Member Presence
1. Window A joins thread
2. Window B sees "User joined" event
3. Window A leaves thread
4. Window B sees "User left" event

### Scenario 4: Message Editing
1. Send message in Window A
2. Edit message using REST endpoint
3. Update appears immediately in Window B

## 🛠️ Troubleshooting

### WebSocket Not Connecting
- Check if server is running on port 8080
- Verify Spring Security is not blocking access
- Check browser console for CORS errors

### Messages Not Broadcasting
- Verify thread ID is correct
- Ensure subscribed to correct thread
- Check that user is member of thread

### Database Not Persisting
- Verify PostgreSQL is running
- Check database connection in `application.yaml`
- Review Hibernate logs for SQL errors

## 📚 Full Documentation

See `WEBSOCKET_IMPLEMENTATION.md` for:
- Complete API reference
- Architecture overview
- Security features
- Performance considerations
- Advanced usage examples

## 🔐 Security Notes

- All WebSocket connections require Spring Security authentication
- Users can only access threads they're members of
- Messages can only be edited/deleted by the sender
- Thread membership is verified on every operation

## 📝 Example: Complete Message Flow

```javascript
// 1. Connect
const client = new MessageThreadWebSocketClient();
await client.connect();

// 2. Subscribe to thread
client.subscribeToThread(1, (message) => {
  console.log('Received:', message);
});

// 3. Register event handlers
client.onMessage((msg) => {
  if (msg.messageType === 'MESSAGE') {
    console.log(`${msg.senderHandle}: ${msg.content}`);
  }
});

// 4. Send a message
client.sendMessage(1, "Hello, everyone!", []);

// 5. Typing indicator
client.sendTypingIndicator(1, true);
setTimeout(() => {
  client.sendTypingIndicator(1, false);
}, 2000);

// 6. Get history when needed
const history = await client.getMessageHistory(1);
console.log(`${history.length} messages loaded`);

// 7. Disconnect when done
client.disconnect();
```

## 📞 Support

For issues or questions about the WebSocket implementation:
1. Check `WEBSOCKET_IMPLEMENTATION.md`
2. Review demo page source code
3. Check browser developer console for errors
4. Review server logs for backend errors

## ✨ Features Summary

✅ Real-time message broadcasting  
✅ Typing indicators  
✅ Member presence tracking  
✅ Message editing and deletion  
✅ Message history persistence  
✅ User authentication and authorization  
✅ Full API documentation  
✅ Interactive demo application  
✅ JavaScript client library  
✅ Production-ready code  

**Happy messaging!** 🎉

