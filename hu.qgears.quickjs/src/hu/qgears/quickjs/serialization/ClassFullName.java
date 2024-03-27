package hu.qgears.quickjs.serialization;

import java.util.List;

import hu.qgears.commons.UtilString;

public class ClassFullName {
	String fqn;
	public ClassFullName(String fqn) {
		this.fqn=fqn;
	}

	public String getPath() {
		StringBuilder ret=new StringBuilder();
		List<String> pieces=UtilString.split(fqn, ".");
		for(int i=0;i<pieces.size()-1;++i)
		{
			ret.append(pieces.get(0));
			ret.append("/");
		}
		ret.append(pieces.get(pieces.size()-1));
		ret.append(".java");
		return ret.toString();
	}

	public String getPackageName() {
		int idx=fqn.lastIndexOf('.');
		return fqn.substring(0, idx);
	}

	public String getSimpleName() {
		int idx=fqn.lastIndexOf('.');
		return fqn.substring(idx+1);
	}

}
