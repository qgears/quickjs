class FastScroll
{
	constructor(dom)
	{
		this.entries={};
		this.entryHeight=20;
		this.dom=dom;
		this.innerDom=document.createElement('div');
		this.innerDom.style.position="relative";
		dom.appendChild(this.innerDom);
		dom.style.overflow="scroll";
		this.dom.onscroll=this.onscroll.bind(this);
		this.optionclickhandler=this.onoptionclick.bind(this);
		this.setEntries([]);
	}
	setClickListener(fun)
	{
		this.clickListener=fun;
	}
	setEntries(array)
	{
		this.options=array;
		this.nEntry=array.length;
		this.innerDom.style.height=(this.nEntry*this.entryHeight)+"px";
		while (this.innerDom.firstChild) {
		    this.innerDom.removeChild(this.innerDom.firstChild);
		}
		this.entries={};
		this.onscroll();
	}
	select(index)
	{
		// console.info("scroll to: "+index);
		this.dom.scrollTo(0, this.entryHeight*index);
	}
	onoptionclick(event)
	{
		if(this.clickListener)
		{
			this.clickListener(event.target.data);
		}else
		{
			console.info("no click listener: "+event.target.data);
		}
	}
	onscroll()
	{
		var firstVisible=Math.floor(this.dom.scrollTop/this.entryHeight);
		var ch=this.dom.clientHeight;
		var nVisible=Math.ceil(ch/this.entryHeight);
		var margin=nVisible+1;
		for(var indexStr in this.entries)
		{
			var index=Number(indexStr);
			if(index<firstVisible-margin || index>firstVisible+nVisible+margin)
			{
				var d=this.entries[index];
				this.innerDom.removeChild(d);
				delete this.entries[index];
			}
		}
		if(ch)
		{
			for(var i=0; i< nVisible; ++i)
			{
				this.makeVisible(firstVisible+i);
			}
		}
	}
	makeVisible(index)
	{
		if(index>=0 && index<this.nEntry)
		{
			if(!this.entries[index])
			{
				var nd=document.createElement('div');
				nd.onclick=this.optionclickhandler;
				nd.data=index;
				var st=nd.style;
				this.entries[index]=nd;
				nd.className="option";
				nd.innerHTML=this.options[index];
				st.position="absolute";
				st.top=(this.entryHeight*index)+"px";
				st.width="100%";
				this.innerDom.appendChild(nd);
			}
		}
	}
}

