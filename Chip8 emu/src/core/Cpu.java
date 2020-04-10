package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import graphics.Canevas;

public class Cpu {
	final static int TAILLEMEMOIRE =  4096;
	final static short ADRESSEDEBUT =  0x200;
	private final static byte[] DEFROM= new byte[]{
			//6XNN mets NN dans VX
			0x61,0x11,
			0x62,0x12,
		    //8XY1 Mets VX à VX OR VY
	  (byte)0x81,0x21,
	};
	
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
	private Keyboard keyboard;
    
    public Cpu(Canevas ecran, Keyboard keyboard) {
    	this.ecran =  ecran;
    	this.keyboard =  keyboard;
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
	        
	        //bArray = DEFROM; // POUR DES TEST
	        
	        //On copie dans la rom
	        for(int i=ADRESSEDEBUT; i<TAILLEMEMOIRE;i++) { //Les 512 premiers octets sont réservés
	        	if(i<bArray.length+ADRESSEDEBUT) {
	            	memoire[i] = bArray[i-ADRESSEDEBUT];
	        	}else {
	        		memoire[i]=0x00;
	        	}
	        }
	        
	        
	        //On charge les polices de 0 à F
	            memoire[0]= (byte)0xF0;memoire[1]= (byte)0x90;memoire[2]= (byte)0x90;memoire[3]= (byte)0x90; memoire[4]= (byte)0xF0; // O 

	            memoire[5]= (byte)0x20;memoire[6]= (byte)0x60;memoire[7]= (byte)0x20;memoire[8]= (byte)0x20; memoire[9]= (byte)0x70; // 1 

	            memoire[10]= (byte)0xF0;memoire[11]= (byte)0x10;memoire[12]= (byte)0xF0;memoire[13]= (byte)0x80; memoire[14]= (byte)0xF0; // 2 

	            memoire[15]= (byte)0xF0;memoire[16]= (byte)0x10; memoire[17]= (byte)0xF0; memoire[18]= (byte)0x10; memoire[19]= (byte)0xF0; // 3 

	            memoire[20]= (byte)0x90; memoire[21]= (byte)0x90; memoire[22]= (byte)0xF0; memoire[23]= (byte)0x10; memoire[24]= (byte)0x10; // 4 

	            memoire[25]= (byte)0xF0; memoire[26]= (byte)0x80; memoire[27]= (byte)0xF0; memoire[28]= (byte)0x10; memoire[29]= (byte)0xF0; // 5 

	            memoire[30]= (byte)0xF0; memoire[31]= (byte)0x80; memoire[32]= (byte)0xF0; memoire[33]= (byte)0x90; memoire[34]= (byte)0xF0; // 6 

	            memoire[35]= (byte)0xF0; memoire[36]= (byte)0x10; memoire[37]= (byte)0x20; memoire[38]= (byte)0x40; memoire[39]= (byte)0x40; // 7 

	            memoire[40]= (byte)0xF0; memoire[41]= (byte)0x90; memoire[42]= (byte)0xF0; memoire[43]= (byte)0x90; memoire[44]= (byte)0xF0; // 8 

	            memoire[45]= (byte)0xF0; memoire[46]= (byte)0x90; memoire[47]= (byte)0xF0; memoire[48]= (byte)0x10; memoire[49]= (byte)0xF0; // 9 

	            memoire[50]= (byte)0xF0; memoire[51]= (byte)0x90; memoire[52]= (byte)0xF0; memoire[53]= (byte)0x90; memoire[54]= (byte)0x90; // A 

	            memoire[55]= (byte)0xE0; memoire[56]= (byte)0x90; memoire[57]= (byte)0xE0; memoire[58]= (byte)0x90; memoire[59]= (byte)0xE0; // B 

	            memoire[60]= (byte)0xF0; memoire[61]= (byte)0x80; memoire[62]= (byte)0x80; memoire[63]= (byte)0x80; memoire[64]= (byte)0xF0; // C 

	            memoire[65]= (byte)0xE0; memoire[66]= (byte)0x90; memoire[67]= (byte)0x90; memoire[68]= (byte)0x90; memoire[69]= (byte)0xE0; // D 

	            memoire[70]= (byte)0xF0; memoire[71]= (byte)0x80; memoire[72]= (byte)0xF0; memoire[73]= (byte)0x80; memoire[74]= (byte)0xF0; // E 

	            memoire[75]= (byte)0xF0; memoire[76]= (byte)0x80; memoire[77]= (byte)0xF0; memoire[78]= (byte)0x80; memoire[79]= (byte)0x80; // F 

	        
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
	
	/*	Listes de opérations qui échouent:
	 * 	9X
	 *  8XY1
	 *  8XY2
	 *  8XY3
	 *  8XY4
	 *  8XY5
	 *  FX55
	 *  FX33
	 *  
	 * */
	
	
	public void cycle() {
		short opcode =  recupererOpcode();
		int instructionNb =  jp.recupererInstructionNb(opcode);
		byte VY = V[getY(opcode)];
		byte VX = V[getX(opcode)];
		switch (instructionNb) {
			case 0 : // Opcode inutile
				System.out.println("[LOG]: Instruction ONNN non supportée et sera ignorée.");
			break;
			
			case 1 : // 00E0	CLS	Efface l'écran.
				ecran.effacerEcran();
				System.out.println("[LOG]: CLS ");
			break;
			
			case 2 : // 00EE	rts:	return from subroutine call
				if(nbrsaut>0) {
					nbrsaut--;
					pc = saut[nbrsaut];
					System.out.println("[LOG]: Return to: " + String.format("%04X", pc));
				}else {
					System.out.println("[WARNING]: Le programme émulé tente de revenir d'une fonction mais la pile de saut est vide.");
				}
			break;
			
			case 3 : // 1NNN	jmp xxx:	Effectue un saut à l'adresse NNN.
				pc = (short)( (getNNN(opcode))-0x0002 );
				System.out.println("[LOG]: Jump to: " + String.format("%04X", pc));
			break;
			
			case 4 : // 2NNN	jsr xxx:	Exécute le sous-programme à l'adresse NNN.
				saut[nbrsaut] = pc;
				pc = (short)( getNNN(opcode)-0x0002 );
				if(nbrsaut<15) {
					System.out.println("[LOG]: Call to: " + String.format("%04X", pc));
					nbrsaut++;
				}else { //Trop de sauts
					System.out.println("[WARNING]: Stack overflow! Plus de 16 sauts effectués.");
				}
			break;
			
			case 5 : // 3XNN	skeq vr,xx: 	Sauter l'instruction suivante si VX est égal à NN.
				System.out.println("[LOG]: Skip if: " + String.format("%02X", V[getX(opcode)])+" == "+ String.format("%02X", getNN(opcode)));
				if(V[getX(opcode)] == getNN(opcode)) {
					pc += 0x0002;
				}
			break;
			
			case 6 : // 4XNN	skne vr,xx: 	Sauter l'instruction suivante si VX et NN ne sont pas égaux.
				System.out.println("[LOG]: Skip if: " + String.format("%02X", V[getX(opcode)])+" != "+ String.format("%02X", getNN(opcode)));
				if(V[getX(opcode)] != getNN(opcode)) {
					pc += 0x0002;
				}
			break;
			
			case 7 : // 5XY0	skeq vr,vy: 	Sauter l'instruction suivante si VX et VY sont égaux. 
				System.out.println("[LOG]: Skip if: " + String.format("%01X", V[getX(opcode)])+" == "+ String.format("%01X", V[getY(opcode)]));
				if(V[getX(opcode)] == V[getY(opcode)]) {
					pc += 0x0002;
				}
			break;
			
			case 8 : // 6XNN	mov vr,xx: 	Mettre NN dans VX 
				System.out.println("[LOG]: Put : " + String.format("%02X",getNN(opcode))+" in V"+String.format("%01X",getX(opcode)));
				V[getX(opcode)] = (byte) (getNN(opcode));
			break;
			
			case 9 : // 7XNN	add vr,vx: 	Ajoute NN à VX. 
				System.out.println("[LOG]: Add : " + String.format("%02X",getNN(opcode))+" to V"+String.format("%01X",getX(opcode)));
				V[(opcode& 0x0F00)>>>8] += (byte) ((opcode& 0xFF));
			break;
			
			case 10 : // 8XY0	mov vr,vy: 	Définit VX à la valeur de VY.
				System.out.println("[LOG]: Move : V" + String.format("%01X",getY(opcode))+" to V"+String.format("%01X",getX(opcode)));
				V[getX(opcode)] = V[getY(opcode)];
			break;
			
			case 11 : // 8XY1	or rx,ry: 	Définit VX à VX OR VY.
				System.out.println("[LOG]: Set : V" + String.format("%01X",getX(opcode))+" to: V" + String.format("%01X",getX(opcode))+" OR V"+String.format("%01X",getY(opcode)));
				V[getX(opcode)] = (byte) (VX | VY);
			break;
			
			case 12 : // 8XY2	and rx,ry: 	Définit VX à VX AND VY.
				System.out.println("[LOG]: Set : V" + String.format("%01X",getX(opcode))+" to: V" + String.format("%01X",getX(opcode))+" AND V"+String.format("%01X",getY(opcode)));
				V[getX(opcode)] = (byte) (VX & VY);
			break;
			
			case 13 : // 8XY3	xor rx,ry: 	Définit VX à VX XOR VY.
				System.out.println("[LOG]: Set : V" + String.format("%01X",getX(opcode))+" to: V" + String.format("%01X",getX(opcode))+" XOR V"+String.format("%01X",getY(opcode)));
				V[getX(opcode)] = (byte) (VX ^ VY);
			break;
			
			case 14 : // 8XY4	add vr,vy: 	ajoute VY à VX. VF est mis à 1 quand il y a un dépassement de mémoire (carry), et à 0 quand il n'y en pas.
				System.out.println("[LOG]: Add : V" + String.format("%01X",getY(opcode))+" to V"+String.format("%01X",getX(opcode)));
				if((VY& 0xff) + (VX& 0xff) > 0xFF) {
					V[0xF] = 0x01;
				}else {
					V[0xF] = 0x00;
				}
				V[getX(opcode)] +=VY;
			break;
			
			case 15:	//8XY5	sub vr,vy	Soustrait VY à VX. VF mit a 0 si il y a emprunt (resultat négatif) 1 sinon
				System.out.println("[LOG]: Substract : V" + String.format("%01X",getY(opcode))+" to V"+String.format("%01X",getX(opcode)));
				if((VX& 0xff)<(VY& 0xff)) {
					V[0xF] = 0x00;
				}else {
					V[0xF] = 0x01;
				}
				V[getX(opcode)] -= VY;
			break;
			
			case 16:	//8XY6  shr vr	(shift) VX à droite de 1 bit. VF est fixé à la valeur du bit de poids faible de VX avant le décalage. 
				V[0xF] = (byte) (VX&0x01);
				System.out.println("[LOG]: Shift : V" + String.format("%01X",getY(opcode))+">>1 / Set VF to V"+String.format("%02X",V[0xF]));
				
				//Cast en non signé
				V[getX(opcode)] = (byte) ((V[getX(opcode)]&0xFF)>>>1);
			break;
			
			case 17:	//8XY7 rsb vr,vy	Soustrait VX à VY. VF mit a 0 si il y a emprunt, 1 sinon
				if((VX& 0xff)>(VY& 0xff)) {
					V[0xF] = 0x00;
				}else {
					V[0xF] = 0x01;
				}
				System.out.println("[LOG]: Substract : V" + String.format("%01X",getX(opcode))+"to V" + String.format("%01X",getY(opcode)));
				V[getY(opcode)] -= VX;
			break;
			
			case 18: //8XYE 	shl vr 	(shift) VX à gauche de 1 bit. VF est fixé à la valeur du bit de poids fort de VX avant le décalage. Si le bit de poids fort de Vx est 1 alors VF =1
				byte mostSignificant = (byte)(V[getX(opcode)] & 0x80);
		        if(mostSignificant!=0){
		            //If 0x10000000 -> set to 0x01
		            mostSignificant = (byte)0x01;
		        }
		        V[0xF] = mostSignificant; //Set VF to the least significant bit of Vx before the shift.
				V[getX(opcode)] = (byte) ((V[getX(opcode)]&0xFF)<<1);
				System.out.println("[LOG]: Shift : V" + String.format("%01X",getY(opcode))+"<<1");
			break;

			case 19: //9XY0		skne rx,ry 		Saute l'instruction suivante si VX != VY
				System.out.println("[LOG]: Jump if : V" + String.format("%01X",getX(opcode))+" != "+ String.format("%01X", V[getY(opcode)]));
				if(VX!=VY) {
					pc+=2;
				}
			break;
			
			case 20:	//ANNN	 mvi xxx:	mets I à NNN
				I = getNNN(opcode);
				System.out.println("[LOG]: Set I to " + String.format("%03X", I));
			break;
			
			case 21:	//BNNN	 jmi xxx:	saute à l'adresse NNN + V0
				pc = (short) (getNNN(opcode)+V[0x00]);
				System.out.println("[LOG]: Jump to " + String.format("%03X", getNNN(opcode)) + " + " + String.format("%02X", V[0x00]));
			break;
			
			case 22:	//CXNN 	 jmi xxx:	Mets VX à un nombre aléatoire inférieur à NN
				V[getX(opcode)] = (byte) ((byte) ((Math.random()* getNN(opcode)))&0x00FF);
				System.out.println("[LOG]: Set V" + String.format("%01X", getX(opcode)) + " to rand	> " + String.format("%03X", getNN(opcode)));
			break;
			
			case 23:	//DXYN sprite rx,ry,s:	 dessine un sprite aux coordonnées (VX, VY).
				this.dessinerEcran(getX(opcode), getY(opcode), (byte) (opcode& 0x000F));
				System.out.println("[LOG]: Draw sprite to " + String.format("%01X", getX(opcode)) + "," + String.format("%01X", getX(opcode)));
			break;
				
			case 24:	//EX9E saute l'instruction suivante si la clé stockée dans VX est pressée.
				System.out.println("[LOG]: Jump if key in V"+String.format("%01X", getX(opcode)) + " is pressed");
				if(keyboard.isKeyPressed(VX)) {
					pc += 0x0002;
				}
			break;
				
			case 25:	//EXA1 saute l'instruction suivante si la clé stockée dans VX n'est pas pressée.
				System.out.println("[LOG]: Jump if key in V"+String.format("%01X", getX(opcode)) + " is not pressed");
				if(!keyboard.isKeyPressed(VX)) {
					pc += 0x0002;
				}
			break;
			
			case 26:	//FX07 définit VX à la valeur du compteur. 
				System.out.println("[LOG]: Set V" + String.format("%01X", getX(opcode)) + " to  " + compteurJeu);
				V[getX(opcode)] = compteurJeu;
			break;
			
			case 27:	//FX0A attend l'appui sur une touche et stocke ensuite la donnée dans VX. 
				V[getX(opcode)] = (byte) keyboard.waitForKeypress();
			break;
			
			case 28:	//FX15 définit la temporisation à VX.
				System.out.println("[LOG]: Set compteurJeu to " + String.format("%02X", VX));
				compteurJeu = VX;
			break;
			
			case 29:	//FX18 définit la minuterie sonore à VX. 
				System.out.println("[LOG]: Set buzzercounter to	  V" + String.format("%01X", getX(opcode)) + " :"+String.format("%01X", VX));
				compteurSon = VX;
			break;
			
			case 30:	//FX1E ajoute VX à I. VF est mis à 1 quand il y a overflow (I+VX>0xFFF), et à 0 si tel n'est pas le cas. 
				System.out.println("[LOG]: Add V" + String.format("%01X", getX(opcode)) + " I");
				if(I+VX>0xFFF){
					V[0xF] = 0x01;
				}else {
					V[0xF] = 0x00;
				}
				I +=VX;
			break;
			
			case 31:	//FX29 définit I à l'emplacement du caractère stocké dans VX. Les caractères 0-F (en hexadécimal) sont représentés par une police 4x5. 
				I=(short) (VX*5); 
			break;
			
			case 32:	//FX33 stocke dans la mémoire le code décimal représentant VX (dans I, I+1, I+2).
				short startmemoryAddr = I;
		        int int_vx = V[getX(opcode)] & 0xff; //Get unsigned int from register Vx

		        int hundreds = int_vx / 100; //Calculate hundreds
		        int_vx = int_vx - hundreds*100;

		        int tens = int_vx/10; //Calculate tens
		        int_vx = int_vx - tens*10;

		        int units = int_vx; //Calculate units

		        memoire[startmemoryAddr] = (byte)hundreds;
		        memoire[startmemoryAddr+1] = (byte)tens;
		        memoire[startmemoryAddr+2] = (byte)units;
			break;
			
			case 33:	//FX55  	 str v0-vr:	 Stocke V0 à VX en mémoire à partir de l'adresse I
				byte X = getX(opcode);
				for(int i=0;i<=X;i++) {
					memoire[I+i] = V[i]; 
				}
			break;
			
			case 34: //FX65 remplit V0 à VX avec les valeurs de la mémoire à partir de l'adresse I. 

				X = getX(opcode);
				for(int i=0;i<=X;i++) {
					  V[i] = memoire[I+i]; 
				}
				
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
	
	private void dessinerEcran(byte x,byte y, byte nibble){  // YA UN SOUCIS LA DEDANS
		byte readBytes = 0;


        byte vf = (byte)0x0;
        while(readBytes < nibble){

            byte currentByte = (byte) memoire[(I +readBytes)]; //Octet du sprite
            for(int i = 0; i <=7; i++){
                    //Pour chaque pixel
                    //Calcule les vraies coordonnées
                    int int_x = V[x] & 0xFF;
                    int int_y = V[y] & 0xFF;
                    int real_x = (int_x + i)%64;
                    int real_y = (int_y + readBytes)%32;
                    
                    // On résupere l'etat du pixel
                    if(ecran.dessinerPixel(getBit(currentByte,7-i),real_x, real_y)){
                        //Un pixel a été effacé
                        vf = (byte)0x01;
                    }

            }

            V[0xF] = vf; //Vf = 1 si 1 pixel effacé
            readBytes++;
        }		
	}
	
	private boolean getBit(byte b, int bit) {
		return (b & (1 << bit)) != 0;
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
