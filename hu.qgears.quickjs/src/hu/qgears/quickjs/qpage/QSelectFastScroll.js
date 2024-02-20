class QSelectFastScroll extends QComponent
{
	addDomListeners()
	{
		this.fs=new FastScroll(this.dom);
//		this.dom.onchange=this.onchange.bind(this);
		this.fs.setClickListener(this.onchange.bind(this));
	}
	onchange(index)
	{
		var fd=this.page.createFormData(this);
		fd.selected=index;
		this.page.send(fd);
	}
	setSelected(value)
	{
		this.fs.select(value);
	}
	setOptions(options)
	{
		this.fs.setEntries(options);
	}
}
