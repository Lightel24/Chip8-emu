package core;

import graphics.Canevas;

public class Cpu {
	final static int TAILLEMEMOIRE =  4096;
	final static short ADRESSEDEBUT =  512;
	
	private byte[] memoire =  new byte[TAILLEMEMOIRE]; 
	byte[] V =   new byte[16]; //le registre 
	short I; //stocke une adresse mémoire ou dessinateur 
    short[] saut =  new short[16]; //pour gérer les sauts dans « mémoire », 16 au maximum 
    byte nbrsaut; //stocke le nombre de sauts effectués pour ne pas dépasser 16 
    byte compteurJeu; //compteur pour la synchronisation 
    byte compteurSon; //compteur pour le son 
    private short pc; //pour parcourir le tableau « mémoire » 
    
    private Canevas ecran;
    private Jumper jp;
    
    public Cpu(Canevas ecran) {
    	this.ecran =  ecran;
    	jp = new Jumper();
    }
    
    public void initialiserCpu() 
    { 
      //On initialise le tout 

        for(int i= 0;i<TAILLEMEMOIRE;i++)
        { 
            getMemoire()[i]= 0; 
        } 

         for(int i= 0;i<16;i++) 
         { 
            V[i]= 0; 
            saut[i]= 0; 
         } 

        setPc(ADRESSEDEBUT); 
        nbrsaut= 0; 
        compteurJeu= 0; 
        compteurSon= 0; 
        I= 0; 

    } 


    public void decompter() 
    { 
        if(compteurJeu>0) 
        compteurJeu--; 

        if(compteurSon>0) 
        compteurSon--; 
    }


	public byte[] getMemoire() {
		return memoire;
	}


	public void setMemoire(byte[] memoire) {
		this.memoire = memoire;
	}


	public short getPc() {
		return pc;
	}


	public void setPc(short pc) {
		this.pc =  pc;
	}

	public void cycle() {
		short opcode =  recupererOpcode();
		int instuctionNb =  jp.recupererInstructionNb(opcode);
		switch (instuctionNb) {
			case 0 : // Opcode inutile
			break;
			case 1 : // 00E0
			break;
		}
		pc+=0x0002;
	}

	private short recupererOpcode(){  
	    return (short) ( (short)(memoire[pc]<<8) | (memoire[pc+1] & 0x00FF)); 
	}
	
	private class Jumper{
		private final int NBROPCODE= 35;
		short[] masque;
		short[] instruction;
		
		public Jumper() {
			  this.masque[0]= (short)  0x0000; this.instruction[0]= (short) 0x0FFF;       	   /* 0NNN */ 
			  this.masque[1]= (short)  0xFFFF; this.instruction[1]= (short) 0x00E0;    		   /* 00E0 */ 
			  this.masque[2]= (short)  0xFFFF; this.instruction[2]= (short) 0x00EE;            /* 00EE */ 
			  this.masque[3]= (short)  0xF000; this.instruction[3]= (short) 0x1000;            /* 1NNN */ 
			  this.masque[4]= (short)  0xF000; this.instruction[4]= (short) 0x2000;            /* 2NNN */ 
			  this.masque[5]= (short)  0xF000; this.instruction[5]= (short) 0x3000;            /* 3XNN */ 
			  this.masque[6]= (short)  0xF000; this.instruction[6]= (short) 0x4000;            /* 4XNN */ 
			  this.masque[7]= (short)  0xF00F; this.instruction[7]= (short) 0x5000;            /* 5XY0 */ 
			  this.masque[8]= (short)  0xF000; this.instruction[8]= (short) 0x6000;            /* 6XNN */ 
			  this.masque[9]= (short)  0xF000; this.instruction[9]= (short) 0x7000;            /* 7XNN */ 
			  this.masque[10]= (short)  0xF00F; this.instruction[10]= (short) 0x8000;          /* 8XY0 */ 
			  this.masque[11]= (short)  0xF00F; this.instruction[11]= (short) 0x8001;          /* 8XY1 */ 
			  this.masque[12]= (short)  0xF00F; this.instruction[12]= (short) 0x8002;          /* 8XY2 */ 
			  this.masque[13]= (short)  0xF00F; this.instruction[13]= (short) 0x8003;          /* BXY3 */ 
			  this.masque[14]= (short)  0xF00F; this.instruction[14]= (short) 0x8004;          /* 8XY4 */ 
			  this.masque[15]= (short)  0xF00F; this.instruction[15]= (short) 0x8005;          /* 8XY5 */ 
			  this.masque[16]= (short)  0xF00F; this.instruction[16]= (short) 0x8006;          /* 8XY6 */ 
			  this.masque[17]= (short)  0xF00F; this.instruction[17]= (short) 0x8007;          /* 8XY7 */ 
			  this.masque[18]= (short)  0xF00F; this.instruction[18]= (short) 0x800E;          /* 8XYE */ 
			  this.masque[19]= (short)  0xF00F; this.instruction[19]= (short) 0x9000;          /* 9XY0 */ 
			  this.masque[20]= (short)  0xF000; this.instruction[20]= (short) 0xA000;          /* ANNN */ 
			  this.masque[21]= (short)  0xF000; this.instruction[21]= (short) 0xB000;          /* BNNN */ 
			  this.masque[22]= (short)  0xF000; this.instruction[22]= (short) 0xC000;          /* CXNN */ 
			  this.masque[23]= (short)  0xF000; this.instruction[23]= (short) 0xD000;          /* DXYN */ 
			  this.masque[24]= (short)  0xF0FF; this.instruction[24]= (short) 0xE09E;          /* EX9E */ 
			  this.masque[25]= (short)  0xF0FF; this.instruction[25]= (short) 0xE0A1;          /* EXA1 */ 
			  this.masque[26]= (short)  0xF0FF; this.instruction[26]= (short) 0xF007;          /* FX07 */ 
			  this.masque[27]= (short)  0xF0FF; this.instruction[27]= (short) 0xF00A;          /* FX0A */ 
			  this.masque[28]= (short)  0xF0FF; this.instruction[28]= (short) 0xF015;          /* FX15 */ 
			  this.masque[29]= (short)  0xF0FF; this.instruction[29]= (short) 0xF018;          /* FX18 */ 
			  this.masque[30]= (short)  0xF0FF; this.instruction[30]= (short) 0xF01E;          /* FX1E */ 
			  this.masque[31]= (short)  0xF0FF; this.instruction[31]= (short) 0xF029;          /* FX29 */ 
			  this.masque[32]= (short)  0xF0FF; this.instruction[32]= (short) 0xF033;          /* FX33 */ 
			  this.masque[33]= (short)  0xF0FF; this.instruction[33]= (short) 0xF055;          /* FX55 */ 
			  this.masque[34]= (short)  0xF0FF; this.instruction[34]= (short) 0xF065;          /* FX65 */ 
		}
		
		public int recupererInstructionNb(short opcode) {
			int id = 0;
			while(id<NBROPCODE || ((masque[id]&opcode) == instruction[id])) {
				++id;
			}
			return id;
		}
	}
}
