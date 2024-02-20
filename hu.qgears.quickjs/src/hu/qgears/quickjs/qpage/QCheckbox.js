class QCheckbox extends QComponent
{
	addDomListeners()
	{
		this.dom.addEventListener("change", this.change.bind(this), false);
	}
	change()
	{
		var fd=this.page.createFormData(this);
		fd.checked=this.dom.checked;
		this.page.send(fd);
	}
	initValue(checked)
	{
		this.dom.checked=checked;
	}
}
