class FileUpload
{
	constructor(file, chunkSize)
	{
		this.file=file;
		this.at=0;
		this.id=""+Math.floor((Math.random() * 1000000000) + 1);
		this.chunksize=chunkSize;
	}
	start()
	{
		this.nextAt=Math.min(this.at+this.chunksize, this.file.size);
		var slice=this.file.slice(this.at, this.nextAt);
		var xhr = new XMLHttpRequest();
		xhr.addEventListener("load", this.onLoad.bind(this));
		// xhr.addEventListener("progress", this.onProgress(this));
		xhr.addEventListener("error", this.onError.bind(this));
		xhr.addEventListener("abort", this.onError.bind(this));
		xhr.upload.addEventListener("progress", this.onProgress.bind(this));
		xhr.open("POST", "upload");
		var formdata = new FormData();
		formdata.append('id', this.id);
		formdata.append('filename', this.file.name);
		formdata.append('filesize', this.file.size);
		formdata.append('start', this.at);
		formdata.append('end', this.nextAt);
		formdata.append('slice', slice);
		xhr.send(formdata);
	}
	onLoad(evt)
	{
		if(evt.target.status==200)
		{
			eval(evt.target.responseText);
		}else
		{
			this.error(this.file, this.at);
		}
	}
	onError()
	{
		this.error(this.file, this.at);
	}
	onProgress(evt)
	{
		this.progress(this.file, this.at+evt.loaded);
	}
	progress(file, bytes)
	{
		console.info("PROGRESS "+bytes+" bytes");
	}
	finished(file)
	{
		console.info("FINISHED");
	}
	error(file, at)
	{
		console.info("ERROR");
	}
}

