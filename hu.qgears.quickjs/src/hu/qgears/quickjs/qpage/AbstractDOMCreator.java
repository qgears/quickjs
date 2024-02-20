package hu.qgears.quickjs.qpage;

import java.io.Writer;

public class AbstractDOMCreator extends HtmlTemplate {
	protected Integer createIndex=null;
	
	public AbstractDOMCreator() {
		super();
	}

	public AbstractDOMCreator(HtmlTemplate parent) {
		super(parent);
	}

	public AbstractDOMCreator(Writer out) {
		super(out);
	}

	public AbstractDOMCreator setCreateIndex(Integer createIndex) {
		this.createIndex = createIndex;
		return this;
	}
}
