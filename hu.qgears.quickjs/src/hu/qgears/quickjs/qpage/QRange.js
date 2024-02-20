class QRange extends QComponent
{
	addDomListeners()
	{
		this.dom.addEventListener("change", this.change.bind(this), false);
	}
	change()
	{
		var fd=this.page.createFormData(this);
		fd.value=this.dom.value;
		this.page.send(fd);
	}
	initValue(value)
	{
		this.dom.value=value;
	}
}
