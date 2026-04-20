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
		this.selectedIndex=0;
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
		this.selectedIndex=Number(index);
		this.dom.scrollTo(0, this.entryHeight*index);
		this.updateSelection();
	}
	onoptionclick(event)
	{
		var dom = event.target;
		this.selectedIndex = -1;	
		while (dom) {
			if (dom.data  === undefined ) {
				dom = dom.parentElement;
			} else {
				this.selectedIndex = dom.data;
				break;
			}
		}
		if(this.clickListener)
		{
			this.clickListener(this.selectedIndex );
		}else
		{
			console.info("no click listener: "+this.selectedIndex );
		}
		this.updateSelection();
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
	updateSelection() {
		if (this.selectedIndex >= 0) {
			for(var idxStr in this.entries) {
				var nd = this.entries[Number(idxStr)];
				var index = nd.data;	
				if (index === this.selectedIndex) {
					nd.className="option-selected";
				} else {
					nd.className="option";
				}
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
				if (index === this.selectedIndex) {
					nd.className="option-selected";
				} else {
					nd.className="option";
				}
				nd.innerHTML=this.options[index];
				st.position="absolute";
				st.top=(this.entryHeight*index)+"px";
				st.width="100%";
				this.innerDom.appendChild(nd);
			}
		}
	}
}

