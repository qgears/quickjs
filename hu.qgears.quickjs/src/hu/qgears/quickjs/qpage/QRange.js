class QRange extends QComponent
{
	addDomListeners()
	{
		this.changeFromServer=false;
		this.dom.addEventListener("change", this.change.bind(this), false);
		this.dom.addEventListener("input", this.input.bind(this), false);
	}
	change()
	{
		if(!this.changeFromServer)
		{
			this.sendState("change");
		}
	}
	input()
	{
		if(!this.changeFromServer)
		{
			this.sendState("input");
		}
	}
	sendState(event)
	{
		var fd=this.page.createFormData(this);
		fd.event=event;
		fd.value=this.dom.value;
		fd.min=this.dom.min;
		fd.max=this.dom.max;
		this.page.send(fd);
	}
	setValue(value, min, max)
	{
		this.changeFromServer=true;
		this.dom.min=min;
		this.dom.max=max;
		this.dom.value=value;
		this.changeFromServer=false;
	}
}
