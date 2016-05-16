# EZSocket
Simple TCP/IP Socket class for JAVA (and Android)

# Dependencies

#### Gradle

	compile 'me.aflak.libraries:ezsocket:1.0'

### Maven

	<dependency>
	  <groupId>me.aflak.libraries</groupId>
	  <artifactId>ezsocket</artifactId>
	  <version>1.0</version>
	  <type>pom</type>
	</dependency>
	
# Use

## Initialise

### From Client

	EZSocket socket = new EZSocket("192.168.0.8", 1234, new EZSocket.EZSocketCallback() {
		@Override
		public void onConnect(EZSocket socket) {
			// socket connected
		}
	
		@Override
		public void onDisconnect(EZSocket socket, String message) {
			// socket disconnected
		}
	
		@Override
		public void onConnectError(final EZSocket socket, String message) {
			// error while connecting
		}
	});
	
### From Server

	EZSocket socket = new EZSocket(server.accept(), new EZSocket.EZSocketDisconnectCallback() {
	    @Override
	    public void onDisconnect(EZSocket socket, String message) {
	    	// socket disconnected
	    }
	});
	
## Fire new event

	socket.emit("your_event", "text", 42, "and as many objects as you want");
	
## Listening for event

	socket.on("your_event", new EZSocket.Listener() {
	    @Override
	    public void onCall(Object... obj) {
	        String text = (String) obj[0];
	        Integer n = (Integer) obj[1];
	        String text = (String) obj[2];
	        
	        Log.d(msg+" : "+String.valueOf(n));
	    }
	});
