# RedisHelper

```java
Planned:

Packet System -> Response Handler (Async AND Not Async responses) -> Exception Handler (Whenever an error occures we will provide a response).
Packet Listening -> Allows you to listen from anywhere.

void sendPacket(Packet packet, String channel);

void sendPacket(Packet packet, String channel, PacketResponseHandler responseHandler);

void sendPacket(Packet packet, String channel, PacketResponseHandler responseHandler, PacketExceptionHander exceptionHandler);

void sendPacket(Packet packet, String channel, PacketExceptionHandler exeptionHandler);

class Packet {
   UUID packetUuid = UUID.randomUUID();
   String sentFrom;
   
   Packet(String sentFrom) {
      this.sentFrom = sentFrom; // On response we know if we need to resend a new packet to make sure they get it back or not.
   }
}

class PacketResponseHandler {
   void handleResponse(Packet packet);
}

// Not sure if we will send back exception handling from other servers (maybe).
class PacketExceptionHandler {
   void handleException(Exception exception);
}
```
