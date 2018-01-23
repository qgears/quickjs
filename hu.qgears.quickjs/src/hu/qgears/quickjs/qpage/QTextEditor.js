class QTextEditor extends QComponent
{
	addDomListeners()
	{
		this.dom.oninput=this.oninput.bind(this);
		this.dom.addEventListener("keyup", this.keyup.bind(this), false);
	}
	keyup(ev)
	{
		//ev.preventDefault();
		if (ev.keyCode === 13) {
			var fd=this.page.createFormData(this);
			fd.append("enter", this.dom.value);
			this.page.send(fd);
		}
	}
	oninput()
	{
		this.dom.className="dirty";
		var fd=this.page.createFormData(this);
		fd.append("text", this.dom.value);
		this.page.send(fd);
	}
	initValue(text)
	{
		this.dom.value=text;
	}
}

