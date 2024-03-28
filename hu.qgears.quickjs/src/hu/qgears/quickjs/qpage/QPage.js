class QDiv extends QComponent
{
	addDomListeners()
	{
		findDomElement()
		{
			return document.getElementById(document.body);
		}
	}
	disposeDom()
	{
		var p=this.dom;
		while (p.hasChildNodes())
			p.removeChild(p.firstChild)
		}
	}
}

