package graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class Canevas extends JPanel{
	public static final int rapport = 8;
	public static final int largeur = 64;
	public static final int hauteur = 32;

	BufferedImage currentFrame;
	byte[][] ecran = new byte[hauteur][largeur]; //tableau des couleurs
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
        if (currentFrame != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.drawImage(currentFrame, 0, 0, this);
            g2d.dispose();
        }
	}
	
	public void initialiserEcran() {
		currentFrame = new BufferedImage(largeur*8, hauteur*8, BufferedImage.TYPE_INT_RGB); 
	}
	public boolean dessinerPixel(int x, int y) {
		if(ecran[x][y] == 0xFF) {
			ecran[x][y] = (byte) 0x00;
		}else {
			ecran[x][y] = (byte) 0xFF;
		}
		return ecran[x][y] == 0xFF;
	}
	public void effacerEcran() {
		for(int y =0; y<currentFrame.getHeight();y++) {
			for(int x =0; x<currentFrame.getWidth();x++) {
				currentFrame.setRGB(x, y, 0);
			}
		}
	}
	public void updateEcran() {
		for(int y =0; y<currentFrame.getHeight();y++) {
			for(int x =0; x<currentFrame.getWidth();x++) {
				currentFrame.setRGB(x, y, ecran[(int) (y/8)][(int) (x/8)]);
			}
		}
		this.repaint();
	}
	
}
