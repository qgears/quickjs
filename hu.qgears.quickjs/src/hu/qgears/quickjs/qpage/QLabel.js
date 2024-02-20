class QLabel extends QComponent
{
	addDomListeners()
	{
	}
	setInnerHtml(text)
	{
		this.controlledNode.innerHTML=text;
	}
}

