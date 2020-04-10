package main;

import javax.swing.JFrame;

import core.Cpu;
import core.Keyboard;
import graphics.Canevas;

public class Main {
	
	private static final int STEP = 4;

	public static void main(String[] args) {
		new Main();
	}
	
	private JFrame fenetre = new JFrame("Chip 8 emu");
	private Canevas canevas = new Canevas();
	private Cpu cpu;
	private Keyboard keyboard;
	boolean running = true;

	public Main(){
		keyboard = new Keyboard(null);
		cpu  = new Cpu(canevas,keyboard);
		cpu.initialiserCpu();
		cpu.load("ressoures\\test_opcode.ch8");
		canevas.initialiserEcran();
		fenetre.setSize((Canevas.largeur+2)*Canevas.rapport,(Canevas.hauteur+5)*Canevas.rapport);
		fenetre.setResizable(false);
		fenetre.setLocationRelativeTo(null);
		fenetre.setContentPane(canevas);
		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fenetre.addKeyListener(keyboard);
		fenetre.setVisible(true);
		canevas.updateEcran();
		run();
	}

	private void run() {
		do {
			for(int i =0;i<10;i++) {
				cpu.cycle();
			}
			cpu.decompter();
			canevas.updateEcran();
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}while(running);
	}
}
