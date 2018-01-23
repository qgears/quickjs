class QButton extends QComponent
{
	addDomListeners()
	{
		this.dom.addEventListener("click", this.onclick.bind(this), false);
	}
	onclick(ev)
	{
		if(ev)
		{
			ev.stopPropagation();
		}
		var fd=this.page.createFormData(this);
		this.page.send(fd);
	}
}
