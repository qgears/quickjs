package hu.qgears.quickjs.utils;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.qpage.QPage;

/**
 * Query the position of an HTML element by its identifier.
 */
public class QueryRectangleById extends HtmlTemplate {
	private hu.qgears.commons.NoExceptionAutoClosable d;
	public SignalFutureWrapper<List<Rectangle>> getRectanglesBySelector(QPage page, String selector)
	{
		SignalFutureWrapper<List<Rectangle>> reply=new SignalFutureWrapper<List<Rectangle>>();
		String replyId="rectangle";
		d=page.getCustomQueryListener2(replyId).addListenerDisposable(
			msg->{
				// System.out.println("rectangleQueryReply: "+msg.header);
				List<Rectangle> ret=new ArrayList<>();
				JSONArray arr=((JSONObject)msg.header).getJSONArray("data");
				for(Object o: arr)
				{
					JSONObject rj=(JSONObject) o;
					Rectangle r=new Rectangle(rj.getInt("x"), rj.getInt("y"), rj.getInt("width"), rj.getInt("height"));
					ret.add(r);
				}
				reply.ready(ret, null);
				d.close();
		});
		page.submitToUI(()->{
			try(ResetOutputObject roo=setParent(page.getCurrentTemplate()))
			{
				write("{\n\tconst ret=[];\n\tconst nodeList = document.querySelectorAll(\"");
				writeJSValue(selector);
				write("\t\");\n\tfor (let i = 0; i < nodeList.length; i++) {\n\t  ret.push(nodeList[i].getBoundingClientRect());\n\t}\n\tglobalQPage.sendCustomJson(\"");
				writeObject(replyId);
				write("\", ret);\n}\n");
			}
		});
		return reply;
	}
	public SignalFutureWrapper<Rectangle> getRectangleById(QPage page, String id)
	{
		return getRectangleBySelector(page, "#"+id);
	}
	public SignalFutureWrapper<Rectangle> getRectangleBySelector(QPage page, String selector)
	{
		SignalFutureWrapper<Rectangle> ret=new SignalFutureWrapper<Rectangle>();
		getRectanglesBySelector(page, selector).addOnReadyHandler(e->{
			Rectangle r=null;
			try {
				r=e.getSimple().get(0);
			} catch (Exception e1) {
			}
			ret.ready(r, e.getThrowable());
		});
		return ret;
	}
}
