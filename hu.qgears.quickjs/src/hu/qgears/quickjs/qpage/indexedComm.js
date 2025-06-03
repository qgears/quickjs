/**
 * Communication library with the following features:
 *  * There are two indexed streams of messages. One on the server one on the client.
 *  * Each message is delivered in their index order. client and server queues are independent of each other.
 *  * Message is stored on the sender side until acknowledgement.
 *  * If connection breaks it is re-created automatically on the client side
 *  ** Unacknowledged messages are re-sent: this way there is no message loss in case of reconnection.
 *  * Designed for WebSocket but may be portable to posts+long polling if necessary
 */

class IndexedComm
{
	/**
	 * Listener events: message, stateChange, error.
	 * states:
	 * * 0:init (temporary state until socket open event received)
	 * * 1:connected (normal state)
	 * * 2:disconnected (retry is going on automatically)
	 * * 3:closed (by close operation or by error)
	 * error: server sends error message: communication channel can't be recovered after this (server side stub does not exits).
	 */
	init(wsUrl, listener)
	{
		this.wsUrl=wsUrl;
		this.sendQueue=[];
		this.state=1;
		this.currentIndex=0;
		this.currentReceiveIndex=0;
		this.retryMinT=10000;
		this.listener=listener;
		this.closed=false;
		this.st=2;
		this.reopenConnection();
		this.pingCtr=0;
		this.pingTimer=setInterval(this.ping.bind(this), 20000);
		return this;
	}
	/**
	 * Keep connection opened whatever happens.
	 */
	ping()
	{
		if(this.closed)
		{
			clearInterval(this.pingTimer);
		}else if(this.st==1)
		{
			try
			{
				this.socket.send(JSON.stringify({ping:this.pingCtr++}));
			}catch(exc)
			{
				console.error(exc);
				console.error("Error connecting URL: "+this.wsUrl);
				// For some reason we do not receive a closed event but only an error when sending ping or other message
				this.rawClose(null);
			}
		}
	}
	setState(st)
	{
		this.st=st;
		this.listener.stateChange(st);
	}
	send(header, ...args)
	{
		const msg={};
		msg.nPart=args.length;
		msg.parts=args;
		msg.index=this.currentIndex++;
		msg.header={};
		msg.header.header=header;
		msg.header.nPart=msg.nPart;
		msg.header.index=msg.index;
		this.sendQueue.push(msg);
		this.reopenConnection();
		if(this.socket.readyState==1)
		{
			this.sendPreparedMessage(msg);
		}
	}
	reopenConnection()
	{
		if(!this.closed && this.st==2)
		{
			console.info("reopen indexedComm connection! "+Date.now()+" "+this.wsUrl);
			this.t0=Date.now();
			this.socket = new WebSocket(this.wsUrl);
			this.socket.addEventListener('message', this.rawMessage.bind(this));
			this.socket.addEventListener('error', this.rawError.bind(this));
			this.socket.addEventListener('close', this.rawClose.bind(this));
			this.socket.addEventListener('open', this.rawOpen.bind(this));
			this.setState(0);
		}
	}
	sendPreparedMessage(msg)
	{
		if(this.socket.readyState==1)
		{
			try
			{
				const str=JSON.stringify(msg.header);
				this.socket.send(str);
				for(const part of msg.parts)
				{
					this.socket.send(part);
				}
			}catch(exc)
			{
				console.error(exc);
				console.error("Error connecting URL: "+this.wsUrl);
				// For some reason we do not receive a closed event but only an error when sending ping or other message
				this.rawClose(null);
			}
		}
	}
	rawOpen(event)
	{
		console.info("WebSocket opened: "+Date.now()+" "+this.wsUrl);
		var i=0;
		for(const msg of this.sendQueue)
		{
			this.sendPreparedMessage(msg);
		}
		this.setState(1);
	}
	rawMessage(event)
	{
		if(this.state==1)
		{
			var m;
			try
			{
				m=JSON.parse(event.data);
			}catch(ex)
			{
				console.error(ex);
				console.error(event.data);
				// This exception breaks to integrity of the channel - close it!
				this.close();
				return;
			}
			if('error' in m)
			{
				console.error('indexedComm Server replies error: ');
				console.error(m);
				this.close();
				return;
			}
			else if('ack' in m)
			{
				// Delete acked messages
				while(this.sendQueue.length!=0 && this.sendQueue[0].index <= m.ack )
				{
					this.sendQueue.shift();
				}
				return;
			}else if('pong' in m)
			{
				// Ignore pong
				return;
			}else
			{
				this.current={header: m};
				this.current.nPart=m.nPart;
				this.current.parts=[];
				this.current.index=m.index;
				this.state=2;
				this.checkMessageTask();
			}
		}else
		{
			this.current.parts.push(event.data);
			this.current.nPart--;
			this.checkMessageTask();
		}
	}
	rawError(event)
	{
		console.error(event);
	}
	rawClose(event)
	{
		console.info("Communication channel closed: "+this.wsUrl);
		console.info(event);
		if(!this.closed)
		{
			this.setState(2);
			const t=Date.now();
			const spent=t-this.t0;
			if(spent>this.retryMinT)
			{
				console.info("Reopen connection - Time spent: "+spent);
				this.reopenConnection();
			}else
			{
				const delay=(this.retryMinT-spent);
				console.info("Reopen connection - Time spent: "+spent+" delay: "+delay);
				setTimeout(this.reopenConnection.bind(this), delay);
			}
		}
	}
	checkMessageTask()
	{
		if(this.current.nPart==0)
		{
			this.sendAck(this.current.index);
			// Never duplicate message
			if(this.current.index>=this.currentReceiveIndex)
			{
				this.currentReceiveIndex=this.current.index+1;
				this.messageReceived(this.current);
				this.current=null;
				this.state=1;
			}
		}
	}
	sendAck(index)
	{
		this.socket.send(JSON.stringify({ack: index}));
	}
	messageReceived(msg)
	{
		this.listener.message(msg.header.header, msg.parts);
	}
	isConnected()
	{
		return this.st==1;
	}
	/**
	 * Dispose current connection and do not open a new one.
	 */
	close()
	{
		try
		{
			this.socket.close();
		}catch(e){console.error(e);}
		this.closed=true;
		this.setState(3);
	}
}
/** In case of client side execution IndexedComm is replaced with this. */
class TeaVMComm
{
	constructor()
	{
		this.st=0;
	}
	init(callback)
	{
		this.callback=callback;
		this.hasRequest=false;
		this.boundProcessMessages=this.processMessages.bind(this);
	}
	processMessages()
	{
		this.hasRequest=false;
		this.callback.processMessages();
	}
	getContextObjectSerialized()
	{
		return new Uint8Array(this.qPageContainer.initialObjectInByteArr);
	}
	getNReplayObject()
	{
		return this.qPageContainer.replayObject.length;
	}
	getReplayObject(i)
	{
		return new Uint8Array(this.qPageContainer.replayObject[i]);
	}
	setQPageContainer(qPageContainer)
	{
		this.qPageContainer=qPageContainer;
		this.url=qPageContainer.createWebSocketUrl('api');
	}
	send(header, ...args)
	{
		this.callback.msgHeader(JSON.stringify(header));
		for(const part of args)
		{
			console.info(part);
			throw Exception("not handled");
			// this.socket.send(part);
		}
	}
	checkConnection()
	{
		if(this.st==0 || this.socket.readyState > 1)
		{
			this.st=1;
			console.info("reopen indexedComm connection! "+Date.now()+" "+this.wsUrl);
			this.t0=Date.now();
			this.socket = new WebSocket(this.url);
			this.socket.addEventListener('message', this.rawMessage.bind(this));
			this.socket.addEventListener('error', this.rawError.bind(this));
			this.socket.addEventListener('close', this.rawClose.bind(this));
			this.socket.addEventListener('open', this.rawOpen.bind(this));
			return false;
		}else
		{
			return true;
		}
	}
	rawOpen(event)
	{
		console.info("WebSocket opened: "+Date.now()+" "+this.url);
		this.callback.channelOpened();
	}
	rawMessage(event)
	{
		// console.info("message");
		// console.info(event);
		// var asArr=new Uint8Array(event.data);
		new Response(event.data).arrayBuffer().then(buffer=> {
			this.callback.messageReceived(new Uint8Array(buffer));
		});
	}
	rawError(event)
	{
		this.callback.channelError();
		console.error(event);
	}
	rawClose(event)
	{
		this.callback.channelClosed();
		console.info("Communication channel closed: "+this.url);
		console.info(event);
	}
	javaMessageBegin()
	{
		this.args=[];
	}
	javaMessageArgString(arg)
	{
		this.args.push(arg);
	}
	javaMessageArgBytes(arg)
	{
		this.args.push(arg);
	}
	javaMessage(header)
	{
		this.qPageContainer.message(header,this.args);
	}
	requestProcessMessages()
	{
		if(!this.hasRequest)
		{
			setTimeout(this.boundProcessMessages,0);
			this.hasRequest=true;
		}
	}
	isConnected()
	{
		return false;
	}
	sendRemoteCall(data)
	{
		this.checkConnection();
		// console.info("sendRemoteCall");
		// console.info(data);
		if(this.st!=0)
		{
			// console.info("Ready state: "+this.socket.readyState);
			if(this.socket.readyState == 1)
			{
				// console.info("socket is connected");
				this.socket.send(new Uint8Array(data));
			}
		}
	}
	requestCommunicationCallback()
	{
		if(this.checkConnection())
		{
			this.callback.channelReady();
		}
	}
}
