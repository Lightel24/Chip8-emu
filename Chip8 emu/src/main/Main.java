package main;

import javax.swing.JFrame;

import core.Cpu;
import graphics.Canevas;

public class Main {
	
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
		cpu.load("D:\\Users\\gabri\\git\\Chip8-emu\\Chip8 emu\\ressoures\\test_opcode.ch8");
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
			for(int i =0;i<4;i++) {
				cpu.cycle();
			}
			canevas.updateEcran();
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}while(running);
	}
}
