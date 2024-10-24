package de.fernuni.kurs01584.ss23.algorithmus;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;



import de.fernuni.kurs01584.ss23.modell.*;


/**
 * Diese Klasse definiert den Suchalgorithmus für Schlangen in einem Dschungel.
 */
public class SchlangenSuche {
	private double abgabeZeit;
	private Dschungel dschungel;
	private Schlangenarten schlangenarten;
	private ArrayList<Schlange> loesung;
	/*private int gmaxPunkte;
	private int gaktPunkte;*/
	private double startZeit;
    private double zeitLimit;
    private int[][] verMat;


    /**
     * Erzeugt eine neue Instanz von SchlangenSuche.
     *
     * @param dschungel     Der Dschungel, in dem die Schlangen gesucht werden.
     * @param schlangenarten Die Liste der Schlangenarten, die im Dschungel vorkommen.
     * @param zeitLimit     Die maximale Zeit, die für die Suche zugewiesen wurde.
     */
	public SchlangenSuche(Dschungel dschungel, Schlangenarten schlangenarten, double zeitLimit){
		this.dschungel = dschungel;
		this.schlangenarten = schlangenarten;
		this.loesung = new ArrayList<Schlange>();
		/*this.gmaxPunkte = 0;
		this.gaktPunkte = 0;*/
		this.zeitLimit = zeitLimit;
		this.abgabeZeit = 0;
	}

	/**
     * Gibt die gefundenen Schlangen zurück.
     *
     * @return Eine Liste der gefundenen Schlangen.
     */
	public ArrayList<Schlange> getLoesung(){
		return this.loesung;
	}
	
	/**
	 * Setzt den Wert des Feldes 'loesung' auf die gegebene ArrayList von Schlange-Objekten.
	 * 
	 * @param neuerLoesung Die neue Lösung, repräsentiert als ArrayList von Schlange-Objekten. 
	 *                     Diese ersetzt die bestehende 'loesung'.
	 */
	public void setLoesung( ArrayList<Schlange> neuerLoesung) {
		this.loesung = neuerLoesung;
	}

	/**
     * Sucht nach Schlangen im Dschungel.
     */
	public void sucheSchlange() {
		startZeit = System.currentTimeMillis();
		System.out.println("Schlange wird gesucht..");

		ArrayList<Feld> tempFelder = new ArrayList<Feld>();
		ArrayList<ArrayList<Feld>> dschungelMatrix = dschungel.getMatrix();

		//eine liste mit alle felder erstellen
		for (ArrayList<Feld> zeile : dschungelMatrix) {
			tempFelder.addAll(zeile);
		}
		//initVerMat();
		//durch schlangenarten iterieren
		
		do {
			//initVerMat();
			ArrayList<Schlange> tempLoesung = new ArrayList<Schlange>();
			for (int h = 0; h < schlangenarten.getSize(); h++) {
				//HIER 31082023
				Schlangenart schlangenart = schlangenarten.getSchlangeByIndex(h);
				//initVerMat();
				//initVerwendbarkeit(tempLoesung);
				//zulaessige Felder die die gleiche zeichen wie die in schlangenart  && feldVerwendbarkeit >0
				

				//sortiere zulaessigeFelder nach punkte
				for (int k = 0; k < schlangenart.getAnzahl(); k++) {// moved from 63 under arraylist declaration
					initVerMat();
					initVerwendbarkeit(tempLoesung);
					
					ArrayList<Feld> zulaessigeFelder = tempFelder.stream()
						    .filter(feld -> (feld.getZeichen() == schlangenart.getZeichenkette().charAt(0) && verMatPos(feld.getZeile(), feld.getSpalte())))
						    .collect(Collectors.toCollection(ArrayList::new));

					Collections.shuffle(zulaessigeFelder);
					Collections.sort(zulaessigeFelder, feldComparator);

					Schlange schlange = new Schlange();

					//iteration durch alle felder
					for (Feld feld : zulaessigeFelder) {

						//rekursion
						//initVerMat();
						Schlangenglied glied = new Schlangenglied(feld);
						int gliedsZeile = glied.getFeld().getZeile();
						int gliedsSpalte = glied.getFeld().getSpalte();
						redVerMat(gliedsZeile, gliedsSpalte);
						
						if(sucheSchlangenGlied(glied, schlangenart, 1) ) {
							schlange.setKopf(glied);
							schlange.setArt(schlangenart.getId());

							

						}else {
							addVerMat(gliedsZeile,gliedsSpalte );
						}

					}
					if (schlange.getKopf().getFeld() != null){
					    // pruefe ob diese lösung bereits da ist
					    long anzahl = tempLoesung.stream().filter(existingSchlange -> existingSchlange.getArt().equals(schlange.getArt())).count();
					    if (anzahl < schlangenart.getAnzahl()) {
					    	
					    	tempLoesung.add(schlange);



					    }

					}


					

				}
				
				
				//HIER ANGEPASST 01092023
				//stoppe den Zeit wenn alle schlangen gefunden sind
				if (loesung.size() == schlangenarten.getAnzahl()){
					double zeit = (System.currentTimeMillis() - startZeit);
					setAbgabeZeit(zeit );
				}


			}
			if( getPunkte(tempLoesung) > getPunkte(getLoesung())) {
				setLoesung(tempLoesung);
				
			}

		}while(loesung.size() != schlangenarten.getAnzahl() && (System.currentTimeMillis() - startZeit < zeitLimit));

		//schreibe zeit am ende
		double zeit = (System.currentTimeMillis() - startZeit);
		setAbgabeZeit( zeit); // in ms
	}

	/**
     * Sucht nach dem nächsten Schlangenglied, das der Schlange hinzugefügt werden soll, Backtracking.
     *
     * @param vorGlied     Das vorherige Schlangenglied.
     * @param schlangenart Die Art der Schlange, die gesucht wird.
     * @param iterator     Der Iterator für die aktuelle Position in der Schlange.
     * @return Gibt true zurück, wenn das nächste Schlangenglied gefunden wurde, sonst false.
     */
	private boolean sucheSchlangenGlied(Schlangenglied vorGlied, Schlangenart schlangenart, int iterator) {

		//n0 die tiefste rekursion
		if(iterator == schlangenart.getSize()) {


			return true;
		}



		//initiere zulaessige nachbarfelder
		ArrayList<Feld> nachbarn = schlangenart.getNachbarschaftsstruktur().getNachbarschaft(dschungel, vorGlied.getFeld())
			    .stream()
			    .filter(feld ->  (verMatPos(feld.getZeile() , feld.getSpalte() )  && feld.getZeichen() == schlangenart.getZeichenkette().charAt(iterator)))
			    .collect(Collectors.toCollection(ArrayList::new));



		//sortiere nach punkte
		Collections.shuffle(nachbarn);
		Collections.sort(nachbarn, feldComparator);

		for (Feld feld : nachbarn) {
			redVerMat(feld.getZeile(), feld.getSpalte());
			Schlangenglied glied = new Schlangenglied(feld);
			vorGlied.setNext(glied);
			//rekursion
			if(sucheSchlangenGlied(glied, schlangenart, iterator + 1) ) {
				return true;

			}else {

				addVerMat( feld.getZeile(), feld.getSpalte());
				vorGlied.setNext(null);
			}

		}
		return false;
	}

	/**
     * Ein Comparator für Felder. Felder werden nach ihrer Punktzahl sortiert.
     */
	Comparator<Feld> feldComparator = new Comparator<Feld>() {
	    @Override
	    public int compare(Feld o1, Feld o2) {
	        // Define your comparison logic here
	    	if (o1.getPunkte() > o2.getPunkte()){
	    		return -1;
	    	}

	    	if (o1.getPunkte() == o2.getPunkte()) {
	    		return 0;
	    	}
	    	return 1;
	    }
	};
	
	//UNBENUTZT
	Comparator<Schlangenart> schlangenartComperator = new Comparator<Schlangenart>() {
	    @Override
	    public int compare(Schlangenart s1, Schlangenart s2) {
	        // Define your comparison logic here
	    	if (s1.getSize() > s2.getSize()){
	    		return -1;
	    	}

	    	if (s1.getSize() == s2.getSize()) {
	    		return 0;
	    	}
	    	return 1;
	    }
	};

	/**
     * Legt die Zeit fest, zu der die Lösung abgegeben wird.
     *
     * @param abgabezeit Die Zeit, zu der die Lösung abgegeben wird.
     */
	public void setAbgabeZeit(double abgabezeit) {
		this.abgabeZeit = abgabezeit;
	}

	/**
     * Gibt die Zeit zurück, zu der die Lösung abgegeben wurde.
     *
     * @return Die Zeit, zu der die Lösung abgegeben wurde.
     */
	public double getAbgabeZeit() {
		// TODO Auto-generated method stub
		return this.abgabeZeit;
	}

	/**
     * Gibt die Anzahl der gefundenen Schlangen zurück.
     *
     * @return Die Anzahl der gefundenen Schlangen.
     */
	public int getAnzahl() {
		// TODO Auto-generated method stub
		return this.loesung.size();
	}

	/**
     * Initialisiert die VerwendbarkeitsMatrix mit den "Verwendbarkeit"Werten der Felder im Dschungel.
     */
	private void initVerMat() {
		 this.verMat = new int[dschungel.getZeilen()][dschungel.getSpalten()] ;
		for (int i = 0;  i < dschungel.getZeilen();i++) {
			for (int j = 0; j < dschungel.getSpalten(); j++) {
				this.verMat[i][j] = dschungel.getFeld(i, j).getVerwendbarkeit();
			}
		}
	}

	/**
     * Überprüft, ob das Feld an der angegebenen Position (x, y) einen positiven "Verwendbarkeit"-Wert hat.
     *
     * @param x Der Zeilenindex des Feldes in der Matrix.
     * @param y Der Spaltenindex des Feldes in der Matrix.
     * @return true, wenn der "Verwendbarkeit"-Wert positiv ist, false sonst.
     */
	private boolean verMatPos(int x, int y) {
		if (verMat[x][y] <= dschungel.getFeld(x, y).getVerwendbarkeit() && verMat[x][y] > 0) {
			return true;
		}

		return false;

	}



	/**
     * Verringert den "Verwendbarkeit"-Wert des Feldes an der angegebenen Position (x, y) um 1.
     * Falls der Wert negativ wird, bleibt er unverändert.
     *
     * @param x Der Zeilenindex des Feldes in der Matrix.
     * @param y Der Spaltenindex des Feldes in der Matrix.
     */
	private void redVerMat(int x, int y) {

			verMat[x][y] -= 1;


	}

	/**
     * Erhöht den "Verwendbarkeit"-Wert des Feldes an der angegebenen Position (x, y) um 1.
     *
     * @param x Der Zeilenindex des Feldes in der Matrix.
     * @param y Der Spaltenindex des Feldes in der Matrix.
     */
	private void addVerMat(int x, int y) {
		verMat[x][y] += 1;
	}
	
	private void print() {
		for (int i = 0; i < verMat.length; i++) {
			for(int j = 0; j < verMat[i].length; j++) {
				System.out.print(verMat[i][j] + " |, ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	/**
	 * Diese Methode initialisiert die Verwendbarkeit der Felder in einer gegebenen Liste von Schlangen.
	 *
	 * @param tempLoesung Eine Liste von Schlangen, deren Felder als verwendet markiert werden sollen.
	 */
	private void initVerwendbarkeit(ArrayList<Schlange> tempLoesung ) {
		if (tempLoesung.size() > 0){
			for (Schlange s : tempLoesung) {
				Schlangenglied gl = s.getKopf();
				int zeile1 = gl.getFeld().getZeile();
				int spalte1 = gl.getFeld().getSpalte();
				redVerMat(zeile1, spalte1);
				
				while ( gl.getNext() != null){
					int zeile = gl.getNext().getFeld().getZeile();
					int spalte = gl.getNext().getFeld().getSpalte();
					redVerMat(zeile, spalte);
					gl = gl.getNext();
				}
			}
		}
	}
	
	/**
	 * Diese Methode berechnet die Gesamtpunktzahl aller Schlangen in einer gegebenen Liste.
	 *
	 * @param schlangenList Eine Liste von Schlangen, deren Punktzahlen addiert werden sollen.
	 * @return Die Gesamtpunktzahl aller Schlangen in der Liste.
	 */
	private int getPunkte(ArrayList<Schlange> schlangenList) {
		int punkte = 0 ;
		if (schlangenList.size() ==0){
			return punkte;
		}
		for (Schlange n : schlangenList) {
			Schlangenglied gl = n.getKopf();
			punkte += gl.getNext().getFeld().getPunkte();
			
			
			while ( gl.getNext() != null){
				punkte += gl.getNext().getFeld().getPunkte();
				
				gl = gl.getNext();
			} 
		}
		return punkte;
		
	}




}
