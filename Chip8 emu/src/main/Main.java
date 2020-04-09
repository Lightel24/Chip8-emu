package main;

import javax.swing.JFrame;

import core.Cpu;
import graphics.Canevas;

public class Main {
	
	private static final int STEP = 4;

	public static void main(String[] args) {
		new Main();
	}
	
	private JFrame fenetre = new JFrame("Chip 8 emu");
	private Canevas canevas = new Canevas();
	private Cpu cpu;
	boolean running = true;

	public Main(){
		cpu  = new Cpu(canevas);
		cpu.initialiserCpu();
		cpu.load("D:\\Users\\gabri\\git\\Chip8-emu\\Chip8 emu\\ressoures\\test_opcode1.ch8");
		canevas.initialiserEcran();
		fenetre.setSize((Canevas.largeur+2)*Canevas.rapport,(Canevas.hauteur+5)*Canevas.rapport);
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
			for(int i =0;i<STEP;i++) {
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
