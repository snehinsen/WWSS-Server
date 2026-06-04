/**
 * WebSocket/REST API Documentation for Message Threads
 *
 * ========================================
 * WebSocket Setup
 * ========================================
 * Connection URL: ws://localhost:8080/ws
 * Protocol: STOMP
 *
 * ========================================
 * Message Endpoints
 * ========================================
 *
 * 1. SEND MESSAGE
 *    Destination: /app/sendMessage
 *    Payload:
 *    {
 *      "content": "Hello",
 *      "attachmentUrls": ["url1", "url2"],
 *      "threadId": 1
 *    }
 *    Broadcasts to: /topic/thread/{threadId}
 *
 * 2. TYPING INDICATOR
 *    Destination: /app/typing
 *    Payload:
 *    {
 *      "threadId": 1,
 *      "isTyping": true
 *    }
 *    Broadcasts to: /topic/thread/{threadId}
 *
 * 3. JOIN THREAD
 *    Destination: /app/joinThread
 *    Payload:
 *    {
 *      "threadId": 1
 *    }
 *    Broadcasts to: /topic/thread/{threadId}
 *
 * 4. LEAVE THREAD
 *    Destination: /app/leaveThread
 *    Payload:
 *    {
 *      "threadId": 1
 *    }
 *    Broadcasts to: /topic/thread/{threadId}
 *
 * ========================================
 * Message Response Format
 * ========================================
 * {
 *   "id": 123,
 *   "senderId": 456,
 *   "senderHandle": "@username",
 *   "senderName": "First Last",
 *   "senderPfp": "url_to_profile_pic",
 *   "threadId": 1,
 *   "content": "Hello",
 *   "attachmentUrls": ["url1"],
 *   "timestamp": "2024-01-01T12:00:00Z",
 *   "messageType": "MESSAGE"
 * }
 *
 * ========================================
 * REST API Endpoints
 * ========================================
 *
 * 1. GET MESSAGE HISTORY
 *    Method: POST
 *    URL: /api/messages/history?threadId=1
 *    Response: List<WebSocketMessageResponse>
 *
 * 2. DELETE MESSAGE
 *    Method: POST
 *    URL: /api/messages/delete?messageId=123
 *    Response: Boolean
 *
 * 3. EDIT MESSAGE
 *    Method: POST
 *    URL: /api/messages/edit?messageId=123&newContent=Updated
 *    Response: Boolean
 *
 * 4. GET DM THREADS
 *    Method: GET
 *    URL: /api/dms
 *    Response: List<ChatThreadDetails>
 *
 * 5. GET THREAD INFO
 *    Method: GET
 *    URL: /api/dms/{threadId}/info?threadId=1
 *    Response: ChatThreadDetails
 *
 * 6. CREATE DM THREAD
 *    Method: POST
 *    URL: /api/dms/create
 *    Payload:
 *    {
 *      "handles": ["@user1", "@user2"],
 *      "tittle": "Group Chat Name"
 *    }
 *    Response: Boolean
 *
 * 7. JOIN THREAD (REST)
 *    Method: POST
 *    URL: /api/dms/join?threadId=1
 *    Response: Boolean
 *
 * 8. LEAVE THREAD (REST)
 *    Method: DELETE
 *    URL: /api/dms/leave?threadId=1
 *    Response: Boolean
 *
 * 9. DELETE THREAD
 *    Method: DELETE
 *    URL: /api/dms/del?threadId=1
 *    Response: Boolean
 *
 * ========================================
 * Event Types
 * ========================================
 * - MESSAGE: Regular chat message
 * - TYPING: User is typing indicator
 * - MESSAGE_DELETED: Message was deleted
 * - MESSAGE_UPDATED: Message was edited
 * - JOINED: User joined thread
 * - LEFT: User left thread
 * - MEMBER_UPDATE: Member join/leave event
 *
 * ========================================
 * Authentication
 * ========================================
 * All endpoints require Spring Security authentication
 * User is extracted from @AuthenticationPrincipal
 * WebSocket connection uses existing Spring session
 *
 * ========================================
 * Error Handling
 * ========================================
 * Errors are sent to /user/queue/errors
 * Format:
 * {
 *   "error": "Error message here"
 * }
 *
 * ========================================
 * Example JavaScript Client
 * ========================================
 * See WebSocketClient.example.js for full implementation
 */

// Marker class for documentation
class `WebSocketAPIDocumentation.md`

