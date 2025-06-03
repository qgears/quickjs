class QProgressBar extends QComponent
{
	addDomListeners()
	{
	}
	setValue(value)
	{
		this.controlledNode.value=value;
		return this;
	}
	setMax(max)
	{
		this.controlledNode.max=max;
		return this;
	}
}

