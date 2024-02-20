class QButton extends QComponent
{
	addDomListeners()
	{
		this.dom.addEventListener("click", this.onclick.bind(this), false);
		this.rcL=this.onrightclick.bind(this);
		this.mdL=this.onmousedown.bind(this);
		this.mouseDownListened=false;
	}
	setInnerHtml(text)
	{
		this.controlledNode.innerHTML=text;
	}
	onclick(ev)
	{
		if(ev)
		{
			ev.stopPropagation();
		}
		var fd=this.page.createFormData(this);
		fd.button=0;
		this.page.send(fd);
	}
	onrightclick(ev)
	{
		if(ev)
		{
		  if (ev.which == 3) {
				ev.stopPropagation();
				if (ev.stopPropagation) ev.stopPropagation();
				ev.cancelBubble = true;
				var fd=this.page.createFormData(this);
				fd.button=3;
				this.page.send(fd);
				return false;
		  }
		}
		return true;
	}
	addRightClickListener(activate)
	{
		if(activate)
		{
//			this.dom.addEventListener("mousedown", this.rcL, false);
			this.dom.oncontextmenu=this.rcL;
		} else {
			this.dom.oncontextmenu=null;
		}
	}
	onmousedown(ev)
	{
		var fd=this.page.createFormData(this);
		fd.button=4;
		this.page.send(fd);
	}
	addMouseDownListener(activate)
	{
		if(activate)
		{
			if(!this.mouseDownListened)
			{
				this.mouseDownListened=true;
				this.dom.addEventListener("mousedown", this.mdL, false);
			}
		} else {
			if(this.mouseDownListened)
			{
				this.mouseDownListened=false;
				this.dom.removeEventListener("mousedown", this.mdL);
			}
		}
	}
}
