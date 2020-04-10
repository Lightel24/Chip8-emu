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
	byte[][] ecran = new byte[largeur][hauteur]; //tableau des couleurs
	
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
		currentFrame = new BufferedImage(largeur*rapport, hauteur*rapport, BufferedImage.TYPE_INT_RGB); 
	}
	public boolean dessinerPixel(boolean bit, int x, int y) {
		boolean previousPixel = ecran[x][y]==(byte)0xFF; //Valeur précedente des pixels
		boolean newPixel = previousPixel ^ bit; //XOR
        
        if(newPixel) {
        	ecran[x][y] = (byte) 0xFF;
        }else {
        	ecran[x][y] = (byte) 0x00;
        }
         
		return previousPixel && !newPixel;
	}
	public void effacerEcran() {
			for(int x =0; x<ecran.length;x++) {
				for(int y =0; y<ecran[x].length;y++) {
		        	ecran[x][y] = (byte) 0x00;
			}
		}
	}
	public void updateEcran() {
		for(int y =0; y<currentFrame.getHeight();y++) {
			for(int x =0; x<currentFrame.getWidth();x++) {
				currentFrame.setRGB(x, y, ecran[(int) (x/rapport)][(int) (y/rapport)]);
			}
		}
		this.repaint();
	}
	
}
