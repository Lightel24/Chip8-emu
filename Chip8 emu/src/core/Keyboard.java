package core;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class Keyboard implements KeyListener{
	
	private static final int[] DEFAULT = new int[]{
			KeyEvent.VK_NUMPAD0,KeyEvent.VK_NUMPAD7,KeyEvent.VK_NUMPAD8,
			KeyEvent.VK_NUMPAD9,KeyEvent.VK_NUMPAD4,KeyEvent.VK_NUMPAD5,
			KeyEvent.VK_NUMPAD6,KeyEvent.VK_NUMPAD1,KeyEvent.VK_NUMPAD2,
			KeyEvent.VK_NUMPAD3,KeyEvent.VK_RIGHT,KeyEvent.VK_COMMA,
			KeyEvent.VK_ASTERISK,KeyEvent.VK_MINUS,KeyEvent.VK_PLUS,KeyEvent.VK_PLUS,KeyEvent.VK_ENTER
	};
	private final int[] keymap;
	private boolean[] keypressed = new boolean[DEFAULT.length];
	private int tampon; // Methode assez laide mais a défaut de trouver une alternative
	
	/*Les valeurs des touches sont données grace à la classe KEYEVENT
	 * Key: chip8
	 * Value: KeyEvent
	*/
	
	public Keyboard(int[] keymap) {	
		if(keymap == null) {
			keymap = DEFAULT;
		}
		this.keymap = keymap;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		int id = getChipKey(arg0.getKeyCode());
		System.out.println("[KEY] The mapped key: "+arg0.getKeyChar()+" has been pressed");
		if(id!=-1) {
			System.out.println("[KEY] The mapped key: "+arg0.getKeyChar()+" has been pressed");
			keypressed[id] = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		int id = getChipKey(arg0.getKeyCode());
		if(id!=-1) {
			System.out.println("[KEY] The mapped key: "+arg0.getKeyChar()+" has been pressed");
			keypressed[id] = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {}
	
	private int getChipKey(int vkey) {
		int i = 0;
		while(i<keymap.length && vkey!=keymap[i]) {
			i++;
		}
		if(i>=keymap.length) {
			i = -1;
		}
		return i;
	}
	
	private boolean isKeyContained(int vkey) {
		return getChipKey(vkey) != -1;
	}
	
	/*https://stackoverflow.com/questions/7081937/java-waiting-for-some-type-of-key-press-to-continue
	* Ne doit pas être invoqué par l'EDT! Risque de deadlock.
	*
    */
	private void setTampon(int valeur) {
		tampon = valeur;
	}

	public int waitForKeypress() {
	    final CountDownLatch latch = new CountDownLatch(1);
	    KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
	    	 public boolean dispatchKeyEvent(KeyEvent e) {
	             if (isKeyContained(e.getKeyCode())) {
	            	setTampon(getChipKey(e.getKeyCode()));
	             	latch.countDown(); // Débloque le thread
	             }
	             return false;
	         }
	    };
	    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
	    try {
			latch.await();	//Le thread attend le callback invoqué par l'EDT
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}  
	    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
	    System.out.println("Touche pressée: " +tampon);
		return tampon;
	}

	public boolean isKeyPressed(byte vX) {
		return keypressed[vX];
	}
	
}
