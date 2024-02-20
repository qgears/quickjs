class QImage extends QComponent
{
	addDomListeners()
	{
	}
	initSrc(text)
	{
		this.dom.src=text;
	}
	onload()
	{
		var fd=this.page.createFormData(this);
		fd.type="onload";
		this.page.send(fd);
	}
	setHasOnloadListener(hasOnloadlistener)
	{
		if(hasOnloadlistener)
		{
			this.dom.onload = this.onload.bind(this);
		}
		else
		{
			this.dom.onload = null;
		}
	}
}

