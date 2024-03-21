package hu.qgears.quickjs.serialization;

import java.util.List;

public class SerializationTemplate extends JavaTemplate {
	CommunicationModel model;
	public SerializationTemplate(CommunicationModel model) {
		this.model=model;
	}

	public String generate() throws Exception {
		List<Class<?>> dtos= model.getDtos();
		write("package ;\npublic class SerializeProject extends ");
		writeClass(SerializeBase.class);
		write("\n{\n\tpublic void serializeObject(value)\n\t{\n\t\tswitch(className)\n\t\t{\n");
		for(Class<?> dot: dtos)
		{
			if(!SerializeBase.handles(dot))
			{
				write("\tcase \"");
				writeObject(dot.getName());
				write("\":\n\t\twriteString(className);\n");
			}
		}
		write("\tdefault:\n\t\treturn super.serializeObject(value);\n\t}\n}\n");
		return ""+getWriter();
	}

}
