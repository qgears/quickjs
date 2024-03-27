package example;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainClass {
    public static void main(String[] args) throws Exception {
/*        var document = HTMLDocument.current();
        var div = document.createElement("div");
        div.appendChild(document.createTextNode("TeaVM generated element"));
        document.getBody().appendChild(div);
        */
        System.out.println("Sysout in TeaVM "+bbwrap());
        refleksuen2();
        /*
        WebSocket ws=WebSocket.create("alma", "korte");
        JSObject jso=null;
        EventListener<CloseEvent> l=new EventListener<CloseEvent>() {
        	@Override
        	public void handleEvent(CloseEvent arg0) {
        		// TODO Auto-generated method stub
        		
        	}
		};
		// EventListener<CloseEvent> l2=a->{return null;};
        ws.onClose(l);
        EventListener<Event> err=new EventListener<Event>() {
        	@Override
        	public void handleEvent(Event arg0) {
        		// TODO Auto-generated method stub
        		
        	}
		};
        ws.onError(err);
        EventListener<MessageEvent> me=new EventListener<MessageEvent>() {
        	@Override
        	public void handleEvent(MessageEvent arg0) {
        		// TODO Auto-generated method stub
        		
        	}
		};
        ws.onMessage(me);
        EventListener<Event> openEv=new EventListener<Event>() {
        	@Override
        	public void handleEvent(Event arg0) {
        		// TODO Auto-generated method stub
        		
        	}
		};
        ws.onOpen(openEv);
        */
    }
	private static void refleksuen2() {
		Class<?> cla=String.class;
		cla=Array.newInstance(cla, 0).getClass();
		Object o=Array.newInstance(cla, 12);
		System.out.println("Arr: "+o);
		System.out.println("Is array: "+o.getClass().isArray());
		System.out.println("Length: "+Array.getLength(o));
		System.out.println("Is array: "+o.getClass().getComponentType().getName());
		// TODO Auto-generated method stub
		
	}
	public static int bbwrap()
	{
		byte[] data=new byte[] {1,2,3,4};
		ByteBuffer bb=ByteBuffer.wrap(data);
		bb.order(ByteOrder.nativeOrder());
		int v=bb.getInt();
		return v;
	}
	/*
	public static void refleksuen() throws Exception
	{
		for(Method m: MyIface.class.getMethods())
		{
			System.out.println("Method name:"+ m.getName());
		}
		ToSerialize ts=new ToSerialize();
		for(Field f: ts.getClass().getDeclaredFields())
		{
			System.out.println("Declared field:"+ f.getName());
			Object v=f.get(ts);
			System.out.println("Declared field value:"+ v);
		}
	}
	*/
}
