class DirReader
{
	constructor(dir, callback)
	{
		this.dir=dir;
		this.callback=callback;
	}
	entriesReceiver(results)
	{
		if(results.length!=0)
		{
			this.callback(this.dir, results);
			this.dirReader.readEntries(this.entriesReceiver.bind(this));
		}else
		{
			// Result length 0 : last part was finished
		}
	}
	start()
	{
		this.dirReader = this.dir.createReader();
		this.dirReader.readEntries(this.entriesReceiver.bind(this));
	}
}

class QFileUpload extends QComponent
{
	constructor(page, id, customId)
	{
		super(page, id); 
		this.customId=customId;
	}
	message(msg)
	{
		const j=JSON.parse(msg);
		if(j.type==="received")
		{
			this.received(j.received);
		}
	}
	stateChange()
	{
		// We do not handle this event for now. Auto reconnect works and feedback also works
	}
	error()
	{
		// We do not handle this event for now.
	}
	addDomListeners()
	{
		this.started=false;
		this.dom.onchange=this.handleChange.bind(this);
		this.queue=[];
		this.chunksize=40960;
		this.maxQueue=409600;
		this.sentBytes=0;
		this.empty=true;
	}
	checkConnectionOpen()
	{
		if(!this.started)
		{
			const url=this.page.createWebSocketUrl()+"&customId="+this.customId;
			this.comm=new IndexedComm().init(url, this);
			this.started=true;
		}
	}
	handleChange(ev)
	{
		const files=this.dom.files;
		this.handleFiles(files);
	}
	handleFiles(files)
	{
		console.info(files);
		var numFiles = files.length;
		console.info("QFileUpload num of files: "+files.length);
		for (var i = 0, numFiles = files.length; i < numFiles; i++) {
			var file = files[i];
			this.handleFile(file);
		}
	}
	handleFile(file)
	{
		this.checkConnectionOpen();
		var msg={};
		msg.type="enqueue";
		msg.filename=this.getFileName(file);
		msg.filesize=file.size;
		console.info("Upload file selected: "+JSON.stringify(msg));
		this.comm.send(msg);
		this.queue.push(file);
		this.startFile();
	}
	getFileName(f)
	{
		if(f.webkitRelativePath)
		{
			return f.webkitRelativePath;
		}
		return f.name;
	}
	startFile()
	{
		if(this.empty)
		{
			if(this.queue.length>0)
			{
				// Queue next file
				this.at=0;
				this.file=this.queue.shift();
				this.empty=false;
				var msg={};
				msg.type="newfile";
				msg.filename=this.getFileName(this.file);
				msg.filesize=this.file.size;
				this.comm.send(msg);
				this.sendFile();
			}else
			{
			}
		}else
		{
		}
	}
	/**
	 * Feedback from server that a message was received.
	 */
	received(nBytes)
	{
		this.sentBytes-=nBytes;
		this.sendFile();
	}
	sendFileSystemEntry(fse)
	{
		if(fse.isFile)
		{
			// console.info("File: "+fse.fullPath);
			// console.info(fse);
			fse.file(this.fileCallback.bind(this));
		}else if(fse.isDirectory)
		{
			var msg={};
			msg.type="createFolder";
			msg.filename=fse.fullPath.substring(1);
			console.info("CreateFolder: "+JSON.stringify(msg));
			this.checkConnectionOpen();
			this.comm.send(msg);
			new DirReader(fse, this.dirReaderCallback.bind(this)).start();
		}
	}
	fileCallback(f)
	{
		this.handleFile(f);
		// console.info(f);
	}
	dirReaderCallback(dir, contents)
	{
		for(var f of contents)
		{
//			console.info(f);
			this.sendFileSystemEntry(f);
		}
//		console.info(contents);
	}
	sendFile()
	{
		while(!this.empty && this.sentBytes<this.maxQueue)
		{
			var msg={};
			msg.type="slice";
			msg.filename=this.file.name;
			msg.filesize=this.file.size;
			msg.at=this.at;
			this.nextAt=Math.min(this.at+this.chunksize, this.file.size);
			const slice=this.file.slice(this.at, this.nextAt);
			this.comm.send(msg, slice);
			this.sentBytes+=slice.size;
			this.at=this.nextAt;
			if(this.at==this.file.size)
			{
				this.empty=true;
			}
		}
		this.startFile();
	}
	installDrop(hostDiv)
	{
		// console.info("Drop zone: "+hostDiv);
		hostDiv.addEventListener("drop", this.drop.bind(this), false);
		hostDiv.addEventListener("dragover", this.dragover.bind(this), false);
		hostDiv.addEventListener("dragend", this.dragend.bind(this), false);
	}
	drop(ev)
	{
		  // console.log("Drop");
		  ev.preventDefault();
		  // If dropped items aren't files, reject them
		  var dt = ev.dataTransfer;
		  // console.log(dt);
		  if(dt.items)
		  {
			var items = dt.items;
			for (var i=0; i<items.length; i++) {
				var item = items[i].webkitGetAsEntry();
				// console.log(item);
				if (item) {
				  this.sendFileSystemEntry(item);
				}
			}
		  }
		  else if (dt.files) {
		  	this.handleFiles(dt.files);
		  }
	}
	dragover(ev)
	{
		// Prevent default select and drag behavior
		ev.preventDefault();
		// console.info("drag over: "+ev.target);
		// ev.target.className="dropping";
	}
	dragend(ev)
	{
		// Remove all of the drag data
	}
}

