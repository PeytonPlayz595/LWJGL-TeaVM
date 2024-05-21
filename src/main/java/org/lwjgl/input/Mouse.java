package org.lwjgl.input;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.teavm.jso.JSBody;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.MouseEvent;
import org.teavm.jso.dom.events.WheelEvent;

import main.Main;


public class Mouse {

	/** Has the mouse been created? */
	private static boolean created;

	private static int x;
	private static int y;

	private static int dx;
	private static int dy;
	private static int dwheel;

	private static String[] buttonName;

	private static final Map<String, Integer> buttonMap = new HashMap<String, Integer>(16);

	private static boolean initialized;
	
	private static int grab_x;
	private static int grab_y;

	private static boolean isGrabbed;
	
	private static LinkedList<MouseEvent> mouseEvents = new LinkedList();
	private static boolean[] buttonStates = new boolean[8];
	private static boolean isInsideWindow = true;
	
	//Make sure it cannot be constructed
	private Mouse() {
	}
	
	public static void setCursorPosition(int new_x, int new_y) {
		//Not possible due to security restrictions
		//Unless you create a custom cursor
		//But I'm not doing that
	}
	
	private static void initialize() {
		buttonName = new String[16];
		for (int i = 0; i < 16; i++) {
			buttonName[i] = "BUTTON" + i;
			buttonMap.put(buttonName[i], i);
		}

		initialized = true;
	}
	
	private static void resetMouse() {
		dx = dy = dwheel = 0;
	}
	
	public static void create() throws LWJGLException {
		try {
			if(created) {
				return;
			}
		
			if(!initialized) {
				initialize();
			}
		
			Main.window.addEventListener("contextmenu", contextmenu = new EventListener<MouseEvent>() {
				@Override
				public void handleEvent(MouseEvent evt) {
					evt.preventDefault();
					evt.stopPropagation();
				}
			});
			Main.canvas.addEventListener("mousedown", mousedown = new EventListener<MouseEvent>() {
				@Override
				public void handleEvent(MouseEvent evt) {
					int b = evt.getButton();
					buttonStates[b == 1 ? 2 : (b == 2 ? 1 : b)] = true;
					mouseEvents.add(evt);
					evt.preventDefault();
					evt.stopPropagation();
				}
			});
			Main.canvas.addEventListener("mouseup", mouseup = new EventListener<MouseEvent>() {
				@Override
				public void handleEvent(MouseEvent evt) {
					int b = evt.getButton();
					buttonStates[b == 1 ? 2 : (b == 2 ? 1 : b)] = false;
					mouseEvents.add(evt);
					evt.preventDefault();
					evt.stopPropagation();
				}
			});
			Main.canvas.addEventListener("mousemove", mousemove = new EventListener<MouseEvent>() {
				@Override
				public void handleEvent(MouseEvent evt) {
					x = getOffsetX(evt);
					y = Main.canvas.getClientHeight() - getOffsetY(evt);
					dx += evt.getMovementX();
					dy += -evt.getMovementY();
					evt.preventDefault();
					evt.stopPropagation();
				}
			});
			Main.canvas.addEventListener("wheel", wheel = new EventListener<WheelEvent>() {
				@Override
				public void handleEvent(WheelEvent evt) {
					mouseEvents.add(evt);
					evt.preventDefault();
					evt.stopPropagation();
					int rotation = evt.getDeltaY() > 0 ? 1 : -1;
					dwheel += rotation;
				}
			});
			Main.canvas.addEventListener("pointerlockchange", pointerLockChange = new EventListener<MouseEvent>() {
				@Override
				public void handleEvent(MouseEvent evt) {
					if(isPointerLocked()) {
						isGrabbed = true;
					} else {
						isGrabbed = false;
					}
				}
			});
			Main.canvas.addEventListener("mouseleave", pointerLockChange = new EventListener<Event>() {
				@Override
				public void handleEvent(Event evt) {
					evt.preventDefault();
					evt.stopPropagation();
					isInsideWindow = false;
				}
			});
			Main.canvas.addEventListener("mouseenter", pointerLockChange = new EventListener<Event>() {
				@Override
				public void handleEvent(Event evt) {
					evt.preventDefault();
					evt.stopPropagation();
					isInsideWindow = true;
				}
			});
			
			created = true;
			
			if(mouseEvents != null) {
				mouseEvents.clear();
			}
		
			setGrabbed(isGrabbed);
		} catch(Throwable t) {
			throw new LWJGLException(t);
		}
	}
	
	public static boolean isCreated() {
		return created;
	}
	
	public static void destroy() {
		if(!created) {
			return;
		}
		
		isGrabbed = false;
		setGrabbed(false);
		created = false;
		
		resetMouse();
		Main.window.removeEventListener("contextmenu", contextmenu);
		Main.window.removeEventListener("mousedown", mousedown);
		Main.window.removeEventListener("mouseup", mouseup);
		Main.window.removeEventListener("mousemove", mousemove);
		Main.window.removeEventListener("wheel", wheel);
		Main.window.removeEventListener("pointerlockchange", pointerLockChange);
		mouseEvents.clear();
	}
	
	public static boolean isButtonDown(int button) {
		return buttonStates[button];
	}
	
	public static String getButtonName(int button) {
		if (button >= buttonName.length || button < 0) {
			return null;
		} else {
			return buttonName[button];
		}
	}
	
	public static int getButtonIndex(String buttonName) {
		Integer ret = buttonMap.get(buttonName);
		if (ret == null) {
			return -1;
		} else {
			return ret;
		}
	}
	
	public static boolean next() {
		currentEvent = null;
		return !mouseEvents.isEmpty() && (currentEvent = mouseEvents.remove(0)) != null;
	}
	
	public static int getEventButton() {
		if(currentEvent == null) return -1;
		int b = currentEvent.getButton();
		return b == 1 ? 2 : (b == 2 ? 1 : b);
	}
	
	public static boolean getEventButtonState() {
		return currentEvent == null ? false : currentEvent.getType().equals(MouseEvent.MOUSEDOWN);
	}
	
	public static int getEventDX() {
		if (currentEvent != null && currentEvent.getType().equals("mousemove")) {
	        return (int) currentEvent.getMovementX();
	    }
	    return 0;
	}
	
	public static int getEventDY() {
		if (currentEvent != null && currentEvent.getType().equals("mousemove")) {
	        return (int) -currentEvent.getMovementY();
	    }
	    return 0;
	}
	
	public static int getEventX() {
		return currentEvent == null ? -1 : currentEvent.getClientX();
	}
	
	public static final int getEventY() {
		return currentEvent == null ? -1 : Main.canvas.getClientHeight() - currentEvent.getClientY();
	}
	
	public static int getEventDWheel() {
		return ("wheel".equals(currentEvent.getType())) ? (((WheelEvent)currentEvent).getDeltaY() == 0.0D ? 0 : (((WheelEvent)currentEvent).getDeltaY() > 0.0D ? -1 : 1)) : 0;
	}
	
	public static int getX() {
		return x;
	}
	
	public static int getY() {
		return y;
	}
	
	public static int getDX() {
		int result = dx;
		dx = 0;
		return result;
	}
	
	public static int getDY() {
		int result = dy;
		dy = 0;
		return result;
	}
	
	public static int getDWheel() {
		int result = dwheel;
		dwheel = 0;
		return result;
	}
	
	//Left, right, wheel button and wheel axis
	//Other buttons are most likely software controlled
	//So I'm just returning the default amount
	public static int getButtonCount() {
		return 4;
	}
	
	public static boolean hasWheel() {
		return true;
	}
	
	public static boolean isGrabbed() {
		return isGrabbed;
	}
	
	public static void setGrabbed(boolean grab) {
		boolean grabbed = isGrabbed;
		isGrabbed = grab;
		if(isCreated()) {
			if(grab && !grabbed) {
				grab_x = x;
				grab_y = y;
				dx = 0;
				dy = 0;
				Main.canvas.requestPointerLock();
			} else if (!grab && grabbed) {
				Main.document.exitPointerLock();
				setCursorPosition(grab_x, grab_y);
			}
			
			resetMouse();
		}
	}
	
	public static void updateCursor() {
	}
	
	public static boolean isInsideWindow() {
		return isInsideWindow;
	}
	
	private static EventListener contextmenu = null;
	private static EventListener mousedown = null;
	private static EventListener mouseup = null;
	private static EventListener mousemove = null;
	private static EventListener wheel = null;
	private static EventListener pointerLockChange = null;
	private static MouseEvent currentEvent = null;
	
	@JSBody(params = { }, script = "return document.pointerLockElement != null;")
	private static native boolean isPointerLocked();
	
	@JSBody(params = { "m" }, script = "return m.offsetX;")
	private static native int getOffsetX(MouseEvent m);
	
	@JSBody(params = { "m" }, script = "return m.offsetY;")
	private static native int getOffsetY(MouseEvent m);
}