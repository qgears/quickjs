class QPageContainer
{
	constructor(identifier, timeoutDispose)
	{
		this.identifier=identifier;
		this.messageindex=0;
		this.serverstateindex=0;
		this.components={};
		this.disposed=false;
		this.state=0;
		this.timeoutDispose=timeoutDispose;
		this.sessionIdParameterAdditional="";
	}
	getComponent(id)
	{
		const ret=this.components[id];
		if(ret===undefined)
		{
			throw new Error("Element with id does not exist: "+id);
		}
		return ret;
	}
	/** Register a showStateCallback callback to show a page offline/page disposed UI.
	 * The callback receives state updates: 0 - before connection
	 * 1 - connection ok
	 * 2 - connection not ok
	 * 3 - disposed
	 */
	setShowStateCallback(showStateCallback)
	{
		this.showStateCallback=showStateCallback;
	}
	message(header, parts)
	{
		// Websocket messages
		if(header==="js")
		{
			var page=this;
			var args=parts;
			try
			{
				eval(parts[0]);
			}catch(exc)
			{
				console.error("Executing: server JS message:");
				console.error(exc);
				console.error(parts[0]);
			}
		}
	}
	stateChange(state)
	{
		if(state==3 && !this.disposed)
		{
			this.dispose("Communication error");
		}
		if(this.state!=state)
		{
			this.state=state;
			this.showState();
		}
	}
	showState()
	{
		if(this.showStateCallback)
		{
			this.showStateCallback(this.state);
		}else
		{
			if(this.state==2)
			{
				console.info("Offline");
			}else if(this.state==3)
			{
				setTimeout(function(){
					var body=document.body;
					var div = document.createElement("div");
					div.style.top = "0%";
					div.style.left = "0%";
					div.style.width = "100%";
					div.style.height = "100%";
					div.style.position = "absolute";
					div.style.color = "white";
					div.style.display="block";
					div.style.zIndex=1001;
					div.style.backgroundColor="rgba(0,0,0,.8)";
					div.innerHTML = "Page is disposed.";
					body.appendChild(div);
				}.bind(this), 2000);
			}
		}
	}
	createWebSocketUrl(websocketName='true')
	{
	
		const url=window.location.origin.replace('http', 'ws')+window.location.pathname+'?websocket='+websocketName+'&QPageContainer='+this.identifier+this.sessionIdParameterAdditional;
		return url;
	}
	setTeaVMCallback(teaVmCallback)
	{
		this.teaVmCallback=teaVmCallback;
		this.comm=new TeaVMComm();
		this.comm.setQPageContainer(this);
		return this.comm;
	}
	setPath(path)
	{
		this.path=path;
	}
	/// Start communication with server and set up global listeners
	/// mode: EQPageMode.ordinal()
	///       0 start in serverside mode: server is accessed through IndexedComm - WebSocket
	///       1 start in hybrid (clientside after initialization) mode: server is not accessed but local TeaVM is accessed with messages
	start(mode)
	{
		const url=this.createWebSocketUrl();
		switch(mode)
		{
		case 0:
			this.comm=new IndexedComm().init(url, this);
			break;
		case 1:
			main([]);
			this.teaVmCallback.createPageContainer(this.identifier, mode);
			this.teaVmCallback.openPath(this.path);
			this.comm.init(this.teaVmCallback);
			break;
		default:
			throw Exception("Mode not handled: "+mode);
		}
		if(this.supports_history_api())
		{
			window.addEventListener("popstate", function(e) {
				var FD = {};
				FD.history_popstate="true";
				FD.pathname=location.pathname;
				FD.search=location.search;
				this.send(FD);
			}.bind(this));
		}
		document.addEventListener("visibilitychange", function() {
			// Even if it is not handled on the server it triggers reconnection
			// in case the client is in a dormant state.
			// This will soon update the UI.
			this.sendCustomJson("visibilitychange", document.visibilityState);
		}.bind(this));
		window.addEventListener('resize', this.windowResized.bind(this));
		this.windowResized();
		var fd=this.createFormData(this);
		fd.type="started";
		this.send(fd);
	}
	windowResized()
	{
		var fd=this.createFormData(this);
		fd.type="windowSize";
		fd.width=window.innerWidth;
		fd.height=window.innerHeight;
		fd.devicePixelRatio=window.devicePixelRatio;
		fd.screenWidth=window.screen.width;
		fd.screenHeight=window.screen.height;
		this.send(fd);
	}
	createFormData(component)
	{
		var FD = {};
		FD.component=component.identifier;
		return FD;
	}
	sendCustomJson(type, jsonObj)
	{
		const toSend={};
		toSend.type=type;
		toSend.data=jsonObj;
		toSend.custom=true;
		// toSend.component=component.identifier;
		this.send(toSend);
	}
	sendJson(type, jsonObj)
	{
		const toSend={};
		toSend.type=type;
		toSend.data=jsonObj;
		// toSend.component=component.identifier;
		this.send(toSend);
	}
	/** Send a JSON message that will land inside a component's user message event. */
	sendUserJson(component, jsonObj)
	{
		const toSend={};
		toSend.user=jsonObj;
		toSend.component=component.identifier;
		this.send(toSend);
	}
	send(msg, ...args)
	{
		if(!this.disposed)
		{
			this.comm.send(msg, ...args);
		}
	}
	beforeUnload()
	{
		var FD = new FormData();
		FD.unload="true";		
		this.send(FD);
		// All further communication is invalid - close the communication object so that it does not reopen!
		this.comm.close();
	}
	resetDisposeTimeout()
	{
		if(this.disposeTimeout)
		{
			clearTimeout(this.disposeTimeout);
		}
		if(this.timeoutDispose!=0)
		{
			this.disposeTimeout=setTimeout(this.disposeByTimeout.bind(this), this.timeoutDispose);
		}
	}
	disposeByTimeout()
	{
		this.dispose("Timeout of server communication loop.");
	}
	dispose(causeMsg)
	{
		console.info("QPage disposed: "+causeMsg);
		// Multiple dispose calls are possible filter to one single - also prevents endless loop
		if(!this.disposed)
		{
			this.disposed=true;
			this.comm.close();
		}
	}
	getNewDomParent()
	{
		if(this.newDomParent)
		{
			return this.newDomParent;
		}
		return document.body;
	}
	setNewDomParent(newDomParent)
	{
		this.newDomParent=newDomParent;
	}
	getFirstRealChildNode(dom)
	{
		var arr=dom.childNodes;
		for(var n in arr)
		{
			var c=arr[n]; 
			if(c.nodeType==1)
			{
				return c;
			}
		}
	}
	setEnableScroll(enable)
	{
		if(!enable)
		{
			document.body.style.height="100%";
			document.body.style.overflow="hidden";
			window.scrollTo(0,0);
		}else
		{
			document.body.style.height="";
			document.body.style.overflow="";
		}
	}
	supports_history_api() {
		return !!(window.history && history.pushState);
	}
	/**
	 * This is appended to the WebSocket queries.
	 * (should have format like: &jsessionid=XYZASDF )
	 */
	setSessionIdParameterAdditional(sessionIdParameterAdditional)
	{
		this.sessionIdParameterAdditional=sessionIdParameterAdditional;
	}
	/**
	 * Create DOM into the tree. See HtmlTemplate.activateCreateDom() 
	 */
	createDom(htmlContent, nameSpaceUri, rootObjectType, methodName, selector, arg1, arg2)
	{
		var div = document.createElementNS(nameSpaceUri, rootObjectType);
		div.innerHTML=htmlContent;
		var nInserted=0;
		for (let i = 0; i < div.childNodes.length; i++) {
			let item = div.childNodes[i];
			if(item instanceof Element || item instanceof HTMLDocument)
			{
				if(nInserted>0)
				{
					console.error(new Error().stack);
					console.error(div.childNodes);
					console.error({error: "Multiple child nodes are created instead of a single one!", dom:htmlContent});
				}
				this.insertDom(item, methodName, selector, arg1, arg2);
				nInserted++;
			}
		}
	}
	insertDom(item, methodName, selector, arg1, arg2)
	{
		switch(methodName) {
			case "QContainer":
				var component=this.components[selector];
				var index=arg1;
				var parentDOM=component.childContainer;
				const next=component.findNextNode(parentDOM, index);
				parentDOM.insertBefore(item, next);
				break;
			case "replaceWith":
				var oldDom=document.querySelector(selector);
				oldDom.replaceWith(item);
				break;
			default:
					console.error(new Error().stack);
					console.error({error: "methodName unknown", methodName: methodName});
					break;
		} 
	}
}
