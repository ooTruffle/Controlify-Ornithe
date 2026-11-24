package dev.isxander.controlify.legacy;

import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.system.MemoryStack;
import pl.tomgirl.pylon.window.DisplaySdl;
import pl.tomgirl.pylon.window.MainThread;

import java.nio.FloatBuffer;

/** Routes controller cursor actions through the mouse events consumed by vanilla screens. */
final class ControllerMenu {
	private static final double WINDOW_HEIGHTS_PER_SECOND = 0.9;

	private long lastFrameNanos;
	private double cursorX;
	private double cursorY;
	private Screen pressedScreen;
	private int pressedButton;

	void moveCursor(float horizontal, float vertical) {
		long now = System.nanoTime();
		int x = Mouse.getX();
		int y = Mouse.getY();
		if (lastFrameNanos == 0L || Math.abs(x - Math.round(cursorX)) > 1 || Math.abs(y - Math.round(cursorY)) > 1) {
			cursorX = x;
			cursorY = y;
		}
		double elapsed = lastFrameNanos == 0L ? 0.0 : Math.min((now - lastFrameNanos) / 1_000_000_000.0, 0.05);
		lastFrameNanos = now;
		if (horizontal == 0.0F && vertical == 0.0F) return;

		int width = Display.getWidth();
		int height = Display.getHeight();
		if (width <= 0 || height <= 0) return;
		double speed = height * WINDOW_HEIGHTS_PER_SECOND / Math.max(1.0, Math.hypot(horizontal, vertical));
		cursorX = Math.clamp(cursorX + horizontal * speed * elapsed, 0.0, width - 1.0);
		// LWJGL mouse coordinates increase upward, while the controller axis increases downward.
		cursorY = Math.clamp(cursorY - vertical * speed * elapsed, 0.0, height - 1.0);
		int nextX = (int) Math.round(cursorX);
		int nextY = (int) Math.round(cursorY);
		warpCursor(nextX, nextY, x, y);
	}

	void positionCursor(int x, int y) {
		int width = Display.getWidth();
		int height = Display.getHeight();
		if (width <= 0 || height <= 0) return;
		int nextX = Math.clamp(x, 0, width - 1);
		int nextY = Math.clamp(y, 0, height - 1);
		cursorX = nextX;
		cursorY = nextY;
		warpCursor(nextX, nextY, Mouse.getX(), Mouse.getY());
	}

	private void warpCursor(int nextX, int nextY, int x, int y) {
		if (nextX != x || nextY != y) {
			Mouse.setCursorPosition(nextX, nextY);
			if (pressedButton != 0) postDragMotion(pressedButton, nextX - x, y - nextY);
		}
	}

	boolean isDragging() {
		return pressedButton != 0;
	}

	void resetMotion() {
		lastFrameNanos = 0L;
	}

	void handleInput(Screen screen, boolean leftPressed, boolean leftHeld,
					 boolean rightPressed, boolean rightHeld, int scroll) {
		if (pressedScreen != null && (pressedScreen != screen
				|| (pressedButton == SDLMouse.SDL_BUTTON_LEFT ? !leftHeld : !rightHeld))) {
			releaseButton();
		}

		if (pressedButton == 0 && (leftPressed || rightPressed)) {
			int button = leftPressed ? SDLMouse.SDL_BUTTON_LEFT : SDLMouse.SDL_BUTTON_RIGHT;
			if (sendMouseEvent(button, true, 0)) {
				pressedButton = button;
				pressedScreen = screen;
			}
		}
		if (scroll != 0) sendMouseEvent(0, false, scroll);
	}

	void releaseButton() {
		if (pressedButton != 0) {
			// Pylon clears its pressed buttons on focus loss. A mouse grab still needs a release.
			if (Display.isActive() && Mouse.isCreated()) {
				sendMouseEvent(pressedButton, false, 0);
			}
			pressedButton = 0;
			pressedScreen = null;
		}
	}

	private static void postDragMotion(int button, int dx, int dy) {
		// A cursor warp can update the position without producing a drag event.
		MainThread.run(() -> {
			long window = DisplaySdl.instance().getHandle();
			if (window == 0L) return;
			try (MemoryStack stack = MemoryStack.stackPush()) {
				FloatBuffer x = stack.mallocFloat(1);
				FloatBuffer y = stack.mallocFloat(1);
				int physicalButtons = SDLMouse.SDL_GetMouseState(x, y);
				SDL_Event event = SDL_Event.calloc(stack);
				event.motion().type(SDLEvents.SDL_EVENT_MOUSE_MOTION).windowID(SDLVideo.SDL_GetWindowID(window))
						.state(physicalButtons | buttonMask(button)).x(x.get(0)).y(y.get(0)).xrel(dx).yrel(dy);
				SDLEvents.SDL_PushEvent(event);
			}
		});
	}

	private static int buttonMask(int button) {
		return button == SDLMouse.SDL_BUTTON_LEFT ? SDLMouse.SDL_BUTTON_LMASK : SDLMouse.SDL_BUTTON_RMASK;
	}

	private static boolean sendMouseEvent(int button, boolean down, int scroll) {
		boolean queued = MainThread.call(() -> {
			long window = DisplaySdl.instance().getHandle();
			if (window == 0L) return false;
			try (MemoryStack stack = MemoryStack.stackPush()) {
				FloatBuffer x = stack.mallocFloat(1);
				FloatBuffer y = stack.mallocFloat(1);
				int physicalButtons = SDLMouse.SDL_GetMouseState(x, y);
				int windowId = SDLVideo.SDL_GetWindowID(window);
				SDL_Event event = SDL_Event.calloc(stack);
				if (scroll != 0) {
					event.wheel().type(SDLEvents.SDL_EVENT_MOUSE_WHEEL).windowID(windowId)
							.mouse_x(x.get(0)).mouse_y(y.get(0)).y(scroll);
				} else {
					if ((physicalButtons & buttonMask(button)) != 0) return false;
					event.button().type(down ? SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN : SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP)
							.windowID(windowId).button((byte) button).down(down).clicks((byte) 1).x(x.get(0)).y(y.get(0));
				}
				return SDLEvents.SDL_PushEvent(event);
			}
		});
		// Deliver it before Screen.handleInputs drains the normal mouse event queue.
		if (queued) Display.processMessages();
		return queued;
	}
}
