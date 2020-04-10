package main;

import javax.swing.JFrame;

import core.Cpu;
import graphics.Canevas;

public class Main {
	
	public static void main(String[] args) {
		byte a = (byte) 0xFF;
		byte b = (byte) 0xFF;
		System.out.println((a <<8)|(b & 0x00FF ));
		
		new Main();
	}
	
	private JFrame fenetre = new JFrame("Chip 8 emu");
	private Canevas canevas = new Canevas();
	private Cpu cpu;
	boolean running = true;

	public Main(){
		cpu  = new Cpu(canevas);
		cpu.initialiserCpu();
		canevas.initialiserEcran();
		fenetre.setSize(Canevas.largeur*Canevas.rapport,Canevas.hauteur*Canevas.rapport);
		fenetre.setResizable(false);
		fenetre.setLocationRelativeTo(null);
		fenetre.setContentPane(canevas);
		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fenetre.setVisible(true);
		canevas.updateEcran();
		run();
	}

	private void run() {
		do {
			
			cpu.cycle();
			canevas.updateEcran();
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}while(running);
	}
}
