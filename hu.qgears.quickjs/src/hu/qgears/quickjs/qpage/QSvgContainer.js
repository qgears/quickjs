class QSvgContainer extends QComponent
{
	addDomListeners()
	{
		this.setControlledNodeSelector(".svgContent");
		
		this.dom.addEventListener('mousedown', this.mousedown.bind(this));
		this.dom.addEventListener('mousemove', this.mousemove.bind(this));
		this.dom.addEventListener('mouseup', this.mouseup.bind(this));
		this.dom.addEventListener('contextmenu', event => event.preventDefault());

		this.state={};
		this.state.pan={};
		this.state.pan.x=0;
		this.state.pan.y=0;
		this.state.scale={};
		this.state.scale.x=1;
		this.state.scale.y=1;

		this.viewBox={};
		this.viewBox.x=0;
		this.viewBox.y=0;
		this.viewBox.w=100;
		this.viewBox.h=100;
		this.width=100;
		this.height=100;

		this.stateCounter=-1;
	}
	initValue(text)
	{
		this.controlledNode.innerHTML=text;
		return this;
	}
	/** Coordinates of the model to be shown on the screen */
	setViewBox(x, y, w, h)
	{
		this.viewBox.x=x;
		this.viewBox.y=y;
		this.viewBox.w=w;
		this.viewBox.h=h;
		this.redraw();
		return this;
	}
	setSize(w, h)
	{
		this.dom.setAttribute("width",""+w+"px");
		this.dom.setAttribute("height",""+h+"px");
		this.width=w;
		this.height=h;
		this.redraw();
		return this;
	}
	mousedown(e)
	{
		switch(e.button)
		{
			case 0:
				this.mouseStart={};
				this.mouseStart.x=e.clientX;
				this.mouseStart.y=e.clientY;
				this.transformBase=JSON.parse(JSON.stringify(this.state));
			break;
			case 2:
				this.scaling={};
				this.scaling.x=e.clientX;
				this.scaling.y=e.clientY;
				this.scaling.layerX=e.layerX;
				this.scaling.layerY=e.layerY;
				this.transformBase=JSON.parse(JSON.stringify(this.state));
			break;
		}
	}
	mousemove(e)
	{
			if(this.mouseStart)
		{
			this.state.pan.x=e.clientX-this.mouseStart.x+this.transformBase.pan.x;
			this.state.pan.y=e.clientY-this.mouseStart.y+this.transformBase.pan.y;
			this.redraw();
		}
		if(this.scaling)
		{
			const xScale=this.countScaleMul(e.clientX-this.scaling.x);
			// console.info("Pan starting point: "+this.scaling.layerX+" transform base x: "+this.transformBase.pan.x+" origin: "+this.scaling.layerX);
			
			this.state.pan.x=this.transformBase.pan.x+
				(this.transformBase.pan.x-this.scaling.layerX)*(xScale-1.0);
			this.state.pan.y=this.transformBase.pan.y+
				(this.transformBase.pan.y-this.scaling.layerY)*(xScale-1.0);
			this.state.scale.x=xScale*this.transformBase.scale.x;
			this.redraw();
		}
	}
	countScaleMul(diffPixel)
	{
		return Math.pow(1.01, diffPixel);
	}
	/** Send the current state to the server.
	    the server will reply the new updated image conforming the current state. */
	sendState()
	{
		this.page.sendUserJson(this, this.state);
		delete this.sender;
	}
	mouseup(e)
	{
		this.mousemove(e);
		if(this.mouseStart)
		{
			delete this.mouseStart;
		}
		if(this.scaling)
		{
			delete this.scaling;
		}
	}
	/** Called by the server to set up the configuration of this viewer. */
	setConfiguration(config)
	{
		if(config.url)
		{
			config.host=this;
			config.onload=function()
			{
				this.host.onload(this);
			}.bind(config);
			config.onerror=function()
			{
				this.host.onerror(this);
			}.bind(config);
			// Configuration of the image is only updated after 
			// the image is loaded. This way there is no flickering.
			
			config.image=new Image();
			config.image.src=config.url;
			config.image.onload=config.onload;
			config.onerror=config.onerror;
		}
		this.dom.style.width=config.width+"px";
		this.dom.style.height=config.height+"px";
	}
	onload(config)
	{
		// Ignore if old image is loaded later than newer image for some reason
		if(config.stateCounter>this.stateCounter)
		{
			this.stateCounter=config.stateCounter
			this.config=config;
			this.redraw();
		}
	}
	onerror(config)
	{
		// TODO handle error
	}
	redraw()
	{
		var x=this.viewBox.x-this.state.pan.x/this.state.scale.x*this.viewBox.w/this.width;
		var y=this.viewBox.y-this.state.pan.y/this.state.scale.x*this.viewBox.h/this.height;
		var w=this.viewBox.w/this.state.scale.x;
		var h=this.viewBox.h/this.state.scale.x;
		this.dom.setAttribute("viewBox", ""+x+" "+y+" "+w+" "+h); 
	}
}

