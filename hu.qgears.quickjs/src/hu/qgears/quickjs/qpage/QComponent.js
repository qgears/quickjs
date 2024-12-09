class QComponent
{
	constructor(page, identifier)
	{
		this.page=page;
		page.components[identifier]=this;
		this.identifier=identifier;
		this.dom=this.findDomElement();
		this.childContainer=this.dom;
		this.controlledNode=this.dom;
		if(!this.dom)
		{
			console.error("Dom object missing: '"+identifier+"' '"+this.constructor.name+"'");
		}
		try
		{
			this.addDomListeners();
		}catch(e)
		{
			console.error("Error adding DOM listeners to QComponent with id: "+identifier);
			console.error(e);
		}
		this.focusListener=this.onfocus.bind(this);
	}
	findDomElement()
	{
		return document.getElementById(this.identifier);
	}
	onfocus()
	{
		var fd=this.page.createFormData(this);
		fd.type="onfocus";
		this.page.send(fd);
	}
	addFocusListener(activate)
	{
		if(activate)
		{
			this.dom.onfocus=this.focusListener;
		} else {
			this.dom.onfocus=null;
		}
	}
	addSizeListener(activate)
	{
		new ResizeObserver(this.onResize.bind(this)).observe(this.dom);
	}
	onResize(e)
	{
		var fd=this.page.createFormData(this);
		fd.type="size";
		fd.clientWidth=this.dom.clientWidth;
		fd.clientHeight=this.dom.clientHeight;
		this.page.send(fd);
	}
	setChildContainer(childContainer)
	{
		this.childContainer=childContainer;
	}
	setChildContainerSelector(selector)
	{
		this.setChildContainer(this.dom.querySelector(selector));
	}
	setControlledNode(controlledNode)
	{
		this.controlledNode=controlledNode;
	}
	setControlledNodeSelector(selector)
	{
		this.setControlledNode(this.dom.querySelector(selector));
	}
	createHTMLInto(htmlContent)
	{
		this.createHTML(this.childContainer, null, htmlContent);
	}
	createHTMLIntoSelector(selector, index, htmlContent)
	{
		const parent=this.dom.querySelector(selector)
		if(parent==null)
		{
			console.error("createHTMLIntoSelector missing parent: "+this.identifier+" "+index+" "+selector);
		}
		this.createHTML(parent, index, htmlContent);
	}
	createHTMLAfterSelector(selector, htmlContent)
	{
		const after=this.dom.querySelector(selector)
		if(after==null)
		{
			console.error("createHTMLIntoSelector missing after: "+this.identifier+" "+selector);
		}
		this.createHTMLAfter(after, htmlContent);
	}
	findNextNode(parentDOM, index)
	{
		var next=null;
		if(index===null)
		{
			next=null;
		}else if(index==-1)
		{
			if(parentDOM.childNodes.length>0)
			{
				next=parentDOM.childNodes[parentDOM.childNodes.length-1];
			}else
			{
				next=null;
			}
		}else
		{
			next=parentDOM.childNodes[index];
		}
		return next;
	}
	createHTML(parentDOM, index, htmlContent)
	{
		var div = document.createElement("div");
		div.innerHTML=htmlContent;
		const next=this.findNextNode(parentDOM, index);
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
				parentDOM.insertBefore(item, next);
				nInserted++;
			}
		}
	}
	createHTMLAfter(after, htmlContent)
	{
		var div = document.createElement("div");
		div.innerHTML=htmlContent;
		var nInserted=0;
		const parentDOM=after.parentNode;
		const next=after.nextSibling;
		for (let i = div.childNodes.length-1; i >=0 ; i--) {
			let item = div.childNodes[i];
			if(item instanceof Element || item instanceof HTMLDocument)
			{
				if(nInserted>0)
				{
					console.error(new Error().stack);
					console.error(div.childNodes);
					console.error({error: "Multiple child nodes are created instead of a single one!", dom:htmlContent});
				}
				parentDOM.insertBefore(item, next);
				nInserted++;
			}
		}
	}
	setParent(newParent, selector, index)
	{
		var c=newParent.childContainer;
		if(selector!=null && selector.length>0)
		{
			c=c.querySelector(selector);
		}
		const next=this.findNextNode(c, index);
		c.insertBefore(this.dom, next);
	}
	createHTMLIntoIndex(index, htmlContent)
	{
		this.createHTML(this.childContainer, index, htmlContent);
	}
	styleAddClass(cla)
	{
		if(!this.dom.classList.contains(cla))
		{
			this.dom.classList.add(cla);
		}
	}
	styleRemoveClass(cla)
	{
		while(this.dom.classList.contains(cla))
		{
			this.dom.classList.remove(cla);
		}
	}
	setDynamicStyle(key, value)
	{
		this.dom.style[key]=value;
	}
	dispose()
	{
		delete this.page.components[this.identifier];
		this.disposeDom();
	}
	disposeDom()
	{
		var p=this.dom.parentNode;
		if(p)
		{
			p.removeChild(this.dom);
		}
	}
	setDisabled(d)
	{
		if(d)
		{
			this.dom.disabled=true;
		}else
		{
			this.dom.removeAttribute("disabled");
		}
		return this; // Chainable setter
	}
	scrollIntoView()
	{
		this.dom.scrollIntoView();
	}
}
