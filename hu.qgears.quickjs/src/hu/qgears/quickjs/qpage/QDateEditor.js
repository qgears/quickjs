class QDateEditor extends QComponent
{
	addDomListeners()
	{
		this.dom.oninput=this.oninput.bind(this);
		this.dom.addEventListener("keyup", this.keyup.bind(this), false);
		this.dom.addEventListener("click", function(ev){if(ev){ev.stopPropagation();}}, false);
	}
	keyup(ev)
	{
		//ev.preventDefault();
		if (ev.keyCode === 13) {
			var fd=this.page.createFormData(this);
			fd.enter=this.dom.value;
			this.page.send(fd);
		}
	}
	oninput()
	{
		this.dom.classList.add("dirty");
		var fd=this.page.createFormData(this);
		fd.text=this.dom.value;
		this.page.send(fd);
	}
	initValue(text)
	{
		this.dom.value=text;
		this.dom.classList.remove("dirty");
	}
}

