class QLink extends QComponent
{
	addDomListeners()
	{
		this.dom.addEventListener('click', function(event) {
			var tag = event.target;
			if (event.button == 0 && this.serverHandled && this.page.supports_history_api()) {
				event.preventDefault();
				var fd=this.page.createFormData(this);
				fd.href=this.dom.href;
				this.page.send(fd);
			}
		}.bind(this));
	}
	setHref(href)
	{
		if(href===null)
		{
			this.dom.removeAttribute("href");
		}else
		{
			this.dom.href=href;
		}
		return this;
	}
	setServerHandled(serverHandled)
	{
		this.serverHandled=serverHandled;
		return this;
	}
}

