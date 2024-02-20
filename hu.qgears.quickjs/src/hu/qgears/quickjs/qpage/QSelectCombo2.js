// TODO https://www.w3schools.com/howto/tryit.asp?filename=tryhow_custom_select
class QSelectCombo2 extends QComponent
{
	addDomListeners()
	{
		this.dom.onchange=this.onchange.bind(this);
	}
	onchange()
	{
		var value=this.dom.options [this.dom.selectedIndex].value;
		var fd=this.page.createFormData(this);
		fd.selected=value;
		this.page.send(fd);
	}
	setSelected(value)
	{
		this.dom.value=value;
	}
	setOptions(options)
	{
		while (this.dom.firstChild) {
		    this.dom.removeChild(this.dom.firstChild);
		}
		var ctr=0;
		for (var i in options){
	    	var opt = document.createElement('option');
	    	opt.value = ctr;
	    	opt.innerHTML = options[i];
	    	ctr++;
//	    	if(ctr<500)
	    	{
		    	this.dom.appendChild(opt);
	    	}
		}
	}
}

