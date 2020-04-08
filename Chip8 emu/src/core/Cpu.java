package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import graphics.Canevas;

public class Cpu {
	final static int TAILLEMEMOIRE =  4096;
	final static short ADRESSEDEBUT =  0x200;
	
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
        	memoire[i]= 0; 
        } 

         for(int i= 0;i<16;i++) 
         { 
            V[i]= 0; 
            saut[i]= 0; 
         } 

        pc=ADRESSEDEBUT; 
        nbrsaut= 0; 
        compteurJeu= 0; 
        compteurSon= 0; 
        I= 0; 

    }
    
    public void load(String filename) {
    	File file = new File(filename);
    	System.out.println("[LOG]: Chargement");
    	 if(file.length()>TAILLEMEMOIRE) {
         	System.err.println("[FATAL ERROR]: Format de la rom incorrect! La taille memoire est dépassée");
         }else {
        	 
	    	FileInputStream fis = null;
	        byte[] bArray = new byte[(int) file.length()];
	        try{
	            fis = new FileInputStream(file);
	            fis.read(bArray);
	            fis.close();        
	            
	        }catch(IOException ioExp){
	            ioExp.printStackTrace();
	        }
	        //On copie dans la rom
	        for(int i=ADRESSEDEBUT; i<TAILLEMEMOIRE;i++) { //Les 512 premiers octets sont réservés
	        	if(i<bArray.length+ADRESSEDEBUT) {
	            	memoire[i] = bArray[i-ADRESSEDEBUT];
	        	}else {
	        		memoire[i]=0x00;
	        	}
	        }
	        for (int i = 0; i < TAILLEMEMOIRE; i++){
	            System.out.println(String.format("%02X", memoire[i]));
	         }
         }

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

	public short getPc() {
		return pc;
	}


	public void setPc(short pc) {
		this.pc =  pc;
	}

	public void cycle() {
		short opcode =  recupererOpcode();
		int instructionNb =  jp.recupererInstructionNb(opcode);
		System.out.println("[LOG]: Instruction: " + String.format("%04X", opcode) + " nb:" + instructionNb);
		byte VY = V[(opcode& 0x00F0)>>>8];
		byte VX = V[(opcode& 0x0F00)>>>8];
		switch (instructionNb) {
			case 0 : // Opcode inutile
				System.out.println("[LOG]: Instruction ONNN non supportée et sera ignorée.");
			break;
			
			case 1 : // 00E0	CLS	Efface l'écran.
				ecran.effacerEcran();
			break;
			
			case 2 : // 00EE	rts:	return from subroutine call
				if(nbrsaut>0) {
					nbrsaut--;
					pc = saut[nbrsaut];
				}else {
					System.out.println("[WARNING]: Le programme émulé tente de revenir d'une fonction mais la pile de saut est vide.");
				}
			break;
			
			case 3 : // 1NNN	jmp xxx:	Effectue un saut à l'adresse NNN.
				pc = (short)( (opcode & 0xFFF)-0x0002 );
			break;
			
			case 4 : // 2NNN	jsr xxx:	Exécute le sous-programme à l'adresse NNN.
				saut[nbrsaut] = pc;
				pc = (short)( (opcode & 0xFFF)-0x2 );
				if(nbrsaut<15) {
					nbrsaut++;
				}else { //Trop de sauts
					System.out.println("[WARNING]: Stack overflow! Plus de 16 sauts effectués.");
				}
			break;
			
			case 5 : // 3XNN	skeq vr,xx: 	Sauter l'instruction suivante si VX est égal à NN.
				if(V[(opcode& 0x0F00)>>>8] == (opcode& 0xFF)) {
					pc += 0x0002;
				}
			break;
			
			case 6 : // 4XNN	skne vr,xx: 	Sauter l'instruction suivante si VX et NN ne sont pas égaux.
				if(V[(opcode& 0x0F00)>>>8] != (opcode& 0x00FF)) {
					pc += 0x0002;
				}
			break;
			
			case 7 : // 5XY0	skeq vr,vy: 	Sauter l'instruction suivante si VX et VY sont égaux. 
				if(V[(opcode& 0x0F00)>>>8] == V[(opcode& 0x00F0)>>>4]) {
					pc += 0x0002;
				}
			break;
			
			case 8 : // 6XNN	mov vr,xx: 	Mettre NN dans VX 
				V[(opcode& 0x0F00)>>>8] = (byte) ((opcode& 0xFF));
			break;
			
			case 9 : // 7XNN	add vr,vx: 	Ajoute NN à VX. 
				V[(opcode& 0x0F00)>>>8] += (byte) ((opcode& 0xFF));
			break;
			
			case 10 : // 8XY0	mov vr,vy: 	Définit VX à la valeur de VY.
				V[(opcode& 0x0F00)>>>8] += (byte) ((opcode& 0xFF));
			break;
			
			case 11 : // 8XY1	or rx,ry: 	Définit VX à VX OR VY.
				V[(opcode& 0x0F00)>>>8] = (byte) (V[(opcode& 0x0F00)>>>8] | V[(opcode& 0x00F0)>>>8]);
			break;
			
			case 12 : // 8XY2	and rx,ry: 	Définit VX à VX AND VY.
				V[(opcode& 0x0F00)>>>8] = (byte) (V[(opcode& 0x0F00)>>>8] & V[(opcode& 0x00F0)>>>8]);
			break;
			
			case 13 : // 8XY3	xor rx,ry: 	Définit VX à VX XOR VY.
				V[(opcode& 0x0F00)>>>8] = (byte) (V[(opcode& 0x0F00)>>>8] ^ V[(opcode& 0x00F0)>>>8]);
			break;
			
			case 14 : // 8XY4	add vr,vy: 	ajoute VY à VX. VF est mis à 1 quand il y a un dépassement de mémoire (carry), et à 0 quand il n'y en pas.
				if(VY + VX > 0xFF) {
					V[0xF] = 0x01;
				}else {
					V[0xF] = 0x00;
				}
				V[(opcode& 0x00F0)>>>8] +=VX;
			break;
			
			case 15:	//8XY5	sub vr,vy	Soustrait VY à VX. VF mit a 0 si il y a emprunt (resultat négatif) 1 sinon
				if(VX<VY) {
					V[0xF] = 0x00;
				}else {
					V[0xF] = 0x01;
				}
				V[(opcode& 0x0F00)>>>8] -= VY;
			break;
				
			case 19: //9XY0		skne rx,ry 		Saute l'instruction suivante si VX != VY
				if(VX!=VY) {
					pc+=2;
				}
			break;
			
			case 20:	//ANNN	 mvi xxx:	mets I à NNN
				I = getNNN(opcode);
			break;
			
			case 23:	//DXYN sprite rx,ry,s:	 dessine un sprite aux coordonnées (VX, VY).
				this.dessinerEcran(getX(opcode), getY(opcode), (byte) (opcode& 0x000F));
			break;
				
				
			default:
				System.out.println("[WARNING]: Instruction inconnue " + String.format("%04X", opcode));
			break;
		}
		
		/* V[(opcode& 0x00F0)>>>8] = VY;
		 V[(opcode& 0x0F00)>>>8] = VX;*/
		
		pc+=0x0002;
	}

	private short recupererOpcode(){  
	    return (short) ( (short)(memoire[pc]<<8) | (memoire[pc+1] & 0x00FF)); 
	}
	
	// On obtient le X : 0X00
	private byte getX(short opcode) {
		return (byte) ( (opcode & 0x0F00) >>> 8);
	}
	
	// On obtient le Y : 00Y0
	private byte getY(short opcode) {
		return (byte) ( (opcode & 0x00F0) >>> 4);
	}
	
	// On obtient le NN : 00NN
	private byte getNN(short opcode) {
		return (byte)(opcode & 0xFF);
	}
	
	// On obtient le NNN : 0NNN
	private short getNNN(short opcode) {
		return (short)(opcode & 0xFFF);
	}
	
	// On obtient le N : 00N0
	private byte getN(short opcode) {
        return (byte) (opcode & 0x00F);
	}
	
	private void dessinerEcran(byte b1,byte b2, byte b3){ 
		byte x=0,y=0,k=0,codage=0,j=0,decalage=0; 
	    V[0xF]=0; 

	     for(k=0;k<b1;k++) 
	        { 
	            codage=memoire[I+k]; //on récupère le codage de la ligne à dessiner 

	            y=(byte) ((V[b2]+k)%Canevas.hauteur); //on calcule l'ordonnée de la ligne à dessiner, on ne doit pas dépasser L 

	            for(j=0,decalage=7;j<8;j++,decalage--) 
	             { 
	                x=(byte) ((V[b3]+j)%Canevas.largeur); //on calcule l'abscisse, on ne doit pas dépasser l 

	                        if(((codage)&(0x1<<decalage))!=0) //on récupère le bit correspondant 
	                        {   //si c'est blanc 
	                            if(ecran.dessinerPixel(x, y)) //le pixel était blanc 
	                            { 
	                                V[0xF]=1; //il y a donc collusion 
	                            } 
	                        } 
	              } 
	        }
	}
	
	private class Jumper{
		private final int NBROPCODE= 35;
		short[] masque = new short[NBROPCODE];
		short[] instruction = new short[NBROPCODE];
		
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
			while(id<NBROPCODE && ((masque[id]&opcode) != instruction[id])) {
				++id;
			}
			return id;
		}
	}
}
