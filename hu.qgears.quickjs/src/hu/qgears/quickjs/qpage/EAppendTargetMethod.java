package hu.qgears.quickjs.qpage;

public enum EAppendTargetMethod {
/*	append,
	appendChild,
	appendChildIndex,
	insertAdjacentElement,
	*/
	/** Selects the container DOM object of the parent QComponent node. (AppendTarget.domSelector is component identifier)
	 * arg1 is the requested index to insert element to.
	 * arg2 is selector within parent (or empty if not used) to navigate into sub-nodes */
	QContainer,
	/** Selects the container DOM object of the parent QComponent node. (AppendTarget.domSelector is component identifier)
	 * arg2 is a selector within parent
	 * Insert after the selected node. */
	QContainer_selector_after,
	/** Replaces the selected node with this DOM subtree (AppendTarget.domSelector is parameter of document.querySelector) */
	replaceWith,
	/** Replace the content of the node with this DOM subtree. */
	replaceContent,
}
