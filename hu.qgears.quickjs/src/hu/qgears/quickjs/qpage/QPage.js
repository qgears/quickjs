class QPage extends QComponent
{
	addDomListeners()
	{
	}
	findDomElement()
	{
		return document.body;
	}
	disposeDom()
	{
		var p=this.dom;
		while (p.hasChildNodes())
		{
			p.removeChild(p.firstChild)
		}
	}
}

