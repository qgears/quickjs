class QPage
{
	constructor(identifier, timeoutDispose)
	{
		this.identifier=identifier;
		this.messageindex=0;
		this.serverstateindex=0;
		this.waitingMessages={};
		this.components={};
		this.disposed=false;
		this.timeoutDispose=timeoutDispose;
	}
	/** Register an on dispose callback to show a different page disposed UI than the default implementation. */
	setDisposeCallback(disposeCallback)
	{
		this.disposeCallback=disposeCallback;
	}
	processServerMessage(serverstate, message)
	{
		if(serverstate==this.serverstateindex)
		{
			message(this);
			this.serverstateindex++;
			while(this.waitingMessages[this.serverstateindex])
			{
				this.waitingMessages[this.serverstateindex](this);
				delete this.waitingMessages[this.serverstateindex];
				this.serverstateindex++;
			}
		}else
		{
			this.waitingMessages[serverstate]=message;
			// TODO out of order server message - init timeout until which it must be processed
		}
	}
	start()
	{
		this.query();
	}
	query()
	{
		var FD = new FormData();
		FD.append("periodic", "true");		
		this.sendPure(FD);
	}
	createFormData(component)
	{
		var FD = new FormData();
		FD.append("component", component.identifier);
		return FD;
	}
	createCustomFormData()
	{
		var FD = new FormData();
		FD.append("custom", "true");
		return FD;
	}
	sendPure(FD)
	{
		if(!this.disposed)
		{
			var xhr = new XMLHttpRequest();
			xhr.qpage=this;
			xhr.responseType = "text";
			xhr.onreadystatechange = function() {
				if (this.readyState == 4) {
					if(this.status == 200)
					{
						var page=this.qpage;
						eval(this.responseText);
					}
					else
					{
						this.qpage.dispose("Server communication XHR fault. Status code: "+this.status);
					}
				}
			}.bind(xhr);
			xhr.open("POST",'?QPage='+this.identifier);
			xhr.send(FD);
		}
	}
	beforeUnload()
	{
		var FD = new FormData();
		FD.append("unload", "true");		
		this.sendPure(FD);
	}
	send(FD)
	{
		FD.append("messageindex", this.messageindex);
		this.messageindex++;
		this.sendPure(FD);
	}
	resetDisposeTimeout()
	{
		if(this.disposeTimeout)
		{
			clearTimeout(this.disposeTimeout);
		}
		this.disposeTimeout=setTimeout(this.disposeByTimeout.bind(this), this.timeoutDispose);
	}
	disposeByTimeout()
	{
		this.dispose("Timeout of server communication loop.");
	}
	dispose(causeMsg)
	{
		console.info("QPage disposed: "+causeMsg);
		// Multiple dispose calls are possible (invalid XHR response+timer) but only show dispose UI once.
		if(!this.disposed)
		{
			this.disposed=true;
			if(this.disposeCallback)
			{
				this.disposeCallback(this, causeMsg);
			}else
			{
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
				div.innerHTML = "Page is disposed. Cause: "+causeMsg;
				body.appendChild(div);
			}
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
}
class QComponent
{
	constructor(page, identifier)
	{
		this.page=page;
		page.components[identifier]=this;
		this.identifier=identifier;
		this.dom=document.getElementById(identifier);
		if(!this.dom)
		{
			console.error("Dom object missing: '"+identifier+"'");
		}
		this.addDomListeners();
	}
	dispose()
	{
		delete this.page.components[this.identifier];
		var p=this.dom.parentNode;
		if(p)
		{
			p.removeChild(this.dom);
		}
	}
}

