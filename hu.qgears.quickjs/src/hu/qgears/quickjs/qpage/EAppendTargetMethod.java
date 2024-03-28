package hu.qgears.quickjs.qpage;

public enum EAppendTargetMethod {
/*	append,
	appendChild,
	appendChildIndex,
	insertAdjacentElement,
	*/
	/** Selects the container DOM object of the parent QComponent node. (AppendTarget.domSelector is component identifier)*/
	QContainer,
	/** Replaces the selected node with this DOM subtree (AppendTarget.domSelector is parameter of document.querySelector) */
	replaceWith,
}
