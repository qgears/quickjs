package hu.qgears.quickjs.qpage;

import hu.qgears.commons.UtilEvent;

public class QProperty<T>
{
	protected T property;

	public QProperty() {
		super();
	}
	public QProperty(T property) {
		super();
		this.property = property;
	}
	public T getProperty() {
		return property;
	}

	public UtilEvent<T> serverChangedEvent=new UtilEvent<>();
	public UtilEvent<T> clientChangedEvent=new UtilEvent<>();
	public void setPropertyFromServer(T property) {
		T pre=getProperty();
		this.property=property;
		boolean changed=false;
		if(pre==null)
		{
			changed=property!=null;
		}else
		{
			changed=!pre.equals(property);
		}
		if(changed)
		{
			serverChangedEvent.eventHappened(property);
		}
	}
	public void setPropertyFromClient(T property) {
		T pre=getProperty();
		this.property=property;
		boolean changed=false;
		if(pre==null)
		{
			changed=property!=null;
		}else
		{
			changed=!pre.equals(property);
		}
		if(changed)
		{
			clientChangedEvent.eventHappened(property);
		}
	}
}
