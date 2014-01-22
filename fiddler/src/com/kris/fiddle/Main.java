package com.kris.fiddle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class Main {

	static char [] alfabet = {	'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',  
								'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	static int [] percent = {	166, 33, 106, 83, 343, 56, 44, 91, 203, 2, 2, 80, 59, 178, 172, 55, 
								6, 161, 165, 215, 65, 35, 20, 12, 30, 1 };
	static int total = count(percent);
	
	static Random rand = new Random();

	public static void main(String[] args) {
		
		// Create a grid
		Letter [][] grid = zeroGrid();

		// add the words
		grid = addWordsToGrid(grid);
		
		// Fill the rest of the grid
		grid = fillGrid(grid);
		
		// print the values
		printGrid(grid);
		
		// Change the values
		ArrayList <Point> done = new ArrayList <Point> ();
		Letter start = null;
		Letter next  = grid[rand.nextInt(grid.length)][rand.nextInt(grid[0].length)];
		for(int i = 0; i < 5; i++){
			// add the value to the string
			start = next;
			next.name = String.valueOf(i).charAt(0);
			done.add(next.coordinate);
			int tries = 0;
			while (done.contains(next.coordinate) && tries < 100){
				next = start.neighbors.get(rand.nextInt(start.neighbors.size()));
			}
		}
		
	}
	
	private static int count(int[] percent) {
		int total = 0;
		for(int i = 0; i < percent.length; i++){ total += percent[i]; }
		return total;
	}

	public static Letter[][] addWordsToGrid(Letter [] [] grid) {
		
		String [] words = {"require", "forfeit", "excuse", "remember", "quantity", "ipsofacto","masculinity"};
		String wordString = "Find the following words: ";
		
		for(int i = 0; i < words.length; i++){

			// get the letters of the word
			char [] letters = words[i].toCharArray();
			LinkedList <Letter> path = new LinkedList <Letter> ();
			int tries = 0;
			Letter current = grid[rand.nextInt(grid.length)][rand.nextInt(grid[0].length)];
			
			// get the path of the word
			for(int j = 0; j < letters.length && tries < 1000; j++){
				// we're starting a new try
				tries++;

				if(j == 0){ // Pick a starting point
					// Pick random start points until you find a valid one.
					current = grid[rand.nextInt(grid.length)][rand.nextInt(grid[0].length)];
					if(current.name == letters[0] || current.name == '0') {
						// if we found something add it to the path
						path.push(current); 
					}else {
						// if not try again
						j--;
					}
				} else { // Pick a point for this point
					
					// Get the list of possible neighbors
					ArrayList <Letter> possibleNeighbors = new ArrayList <Letter>();
					for(int k = 0; k < current.neighbors.size(); k++){
						Letter neighbor = current.neighbors.get(k);
						if((neighbor.name == '0' || neighbor.name == letters[j]) && !path.contains(neighbor)){
							possibleNeighbors.add(neighbor);
						}
					}
					
					// Pick a neighbor
					if(possibleNeighbors.size() == 0){
						// If there is no possible way to go, then go back and try again.
						current = path.pop();
						j--;
					} else {
						current = possibleNeighbors.get(rand.nextInt(possibleNeighbors.size()));
						path.push(current);
					}
				}
			}
			
			// change the path values in the grid
			if(letters.length == path.size()){
				wordString += words[i] + ", ";
				for(int j = letters.length-1; j >= 0; j--){
					
					path.pop().name = letters[j];
				}
			}
		}

		System.out.println(wordString.subSequence(0, wordString.length() - 2) + ".");
		
		return grid;
	}

	public static Letter[][] addWordLinesToGrid(Letter [] [] grid) {
		
		String [] words = {"require", "forfeit", "excuse", "remember", "quantity", "ipsofacto","masculinity"};
		String wordString = "Find the following words: ";
		
		for(int i = 0; i < words.length; i++){

			// get the letters of the word
			char [] letters = words[i].toCharArray();
			LinkedList <Letter> path = new LinkedList <Letter> ();
			int tries = 0;
			Letter current = grid[rand.nextInt(grid.length)][rand.nextInt(grid[0].length)];
			
			// get the path of the word
			for(int j = 0; j < letters.length && tries < 1000; j++){
				// we're starting a new try
				tries++;

				if(j == 0){ // Pick a starting point
					// Pick random start points until you find a valid one.
					current = grid[rand.nextInt(grid.length)][rand.nextInt(grid[0].length)];
					if(current.name == letters[0] || current.name == '0') {
						// if we found something add it to the path
						path.push(current); 
					}else {
						// if not try again
						j--;
					}
				} else { // Pick a point for this point
					
					
					// Get the list of possible neighbors
					ArrayList <Letter> possibleNeighbors = new ArrayList <Letter>();
					for(int k = 0; k < current.neighbors.size(); k++){
						Letter neighbor = current.neighbors.get(k);
						if((neighbor.name == '0' || neighbor.name == letters[j]) && !path.contains(neighbor)){
							possibleNeighbors.add(neighbor);
						}
					}
					
					// Pick a neighbor
					if(possibleNeighbors.size() == 0){
						// If there is no possible way to go, then go back and try again.
						current = path.pop();
						j--;
					} else {
						current = possibleNeighbors.get(rand.nextInt(possibleNeighbors.size()));
						path.push(current);
					}
				}
			}
			
			// change the path values in the grid
			if(letters.length == path.size()){
				wordString += words[i] + ", ";
				for(int j = letters.length-1; j >= 0; j--){
					
					path.pop().name = letters[j];
				}
			}
		}

		System.out.println(wordString.subSequence(0, wordString.length() - 2) + ".");
		
		return grid;
	}

	public static Letter[][] zeroGrid() {
		
		Letter [][] letters = new Letter [10] [10];
		
		for(int i = 0; i < letters.length; i++){
			for (int j = 0; j < letters[i].length; j++){
				letters[i][j] = new Letter('0', new Point(i, j));
			}
		}
		
		// Add the neighbor relationships
		for(int i = 0; i < letters.length; i++){
			for (int j = 0; j < letters[i].length; j++){
				// Let's be lazy and hard code the values 
				if(i > 0 && j > 0){ 									letters[i][j].addNeighbor(letters[i - 1][j - 1]); }
				if(i > 0){ 												letters[i][j].addNeighbor(letters[i - 1][j]); }
				if(i > 0 && j < letters[i].length-1){ 					letters[i][j].addNeighbor(letters[i - 1][j + 1]); }
				if(j > 0){ 												letters[i][j].addNeighbor(letters[i][j - 1]); }
				if(i < letters.length-1 && j > 0){ 						letters[i][j].addNeighbor(letters[i + 1][j - 1]); }
				if(i < letters.length-1){ 								letters[i][j].addNeighbor(letters[i + 1][j]); }
				if(i < letters.length-1 && j < letters[i].length-1){ 	letters[i][j].addNeighbor(letters[i + 1][j + 1]); }
				if(j < letters[i].length-1){ 							letters[i][j].addNeighbor(letters[i][j + 1]); }
			}
		}
		
		return letters;
	}

	public static Letter [][] fillGrid(Letter [][] letters){

		// Fill the letters
		for(int i = 0; i < letters.length; i++){
			for (int j = 0; j < letters[i].length; j++){
				if(letters[i][j].name == '0'){ letters[i][j].name = pickLetter(); }
			}
		}
		
		return letters;
	}
	
	public static void printGrid(Letter[][] letters){
		// Print the content
		String output = "";
		for(int i = 0; i < letters.length; i++){
			for (int j = 0; j < letters[i].length; j++){
				output += letters[i][j].name + " ";
			}
			System.out.println(output);
			output = "";
		}
		System.out.println(output);
	}
	
	public static char pickLetter(){
		char response = '0';
		int pick = rand.nextInt(total);
		int group = 0;
		for(int j = 0; j < alfabet.length; j++){
			group += percent[j];
			if(pick < group){ response = alfabet[j]; break; }
		}
		
		return response;
	}

	public static void pickLetter2(){
		String x = "Evidence-basedz research and practice was developed initially in";
		x += "medicine because research indicated that expert opinion based";
		x += "medical advice was not as reliable as advice based on the accumu-";
		x += "lation of results from scientific experiments. Since then many do-";
		x += "mains have adopted this approach, e.g. Criminology, Social policy,";
		x += "Economics, Nursing etc. Based on Evidence-based medicine, the";
		x += "goal of Evidence-based Software Engineering is:";
		x += "To provide the means by which current best evidence from";
		x += "research can be integrated with practical experience and";
		x += "human values in the decision making process regarding the";
		x += "development and maintenance of software¡± [5].";
		x += "In this context, evidence is defined as a synthesis of best quality";
		x += "scientific studies on a specific topic or research question. The main";
		x += "method of synthesis is a systematic literature review (SLR). In con-";
		x += "trast to an expert review using ad hoc literature selection, an SLR";
		x += "is a methodologically rigorous review of research results. The";
		x += "aim of an SLR is not just to aggregate all existing evidence on a re-";
		x += "search question; it is also intended to support the development of";
		x += "evidence-based guidelines for practitioners. The end point of EBSE";
		x += "is for practitioners to use the guidelines to provide appropriate";
		x += "software engineering solutions in a specific context.";
		x += "The purpose of this study is to review the current status of EBSE";
		x += "since 2004 using a tertiary study to review articles related to EBSE";
		x += "and, in particular, we concentrate on articles describing systematic";
		x += "literature reviews (SLRs). Although SLRs are not synonymous with";
		x += "EBSE, the aggregation of research results is an important part of the";
		x += "EBSE process and, furthermore, is the part of the EBSE process that";
		x += "can be readily observed in the scientific literature. We describe our";
		x += "methodology in Section 2 and present our results in Section 3. In";
		x += "Section 4 we answer our 4 major research questions. We present";
		x += "our conclusions in Section 5.";
		x += "This theorem reduces the task of solving a linear program to that of searching";
		x += "over basic feasible solutions. Since for a problem having n variables and m";
		x += "constraints there are at most";
		x += "basic solutions (corresponding to the number of ways of selecting m of n columns),";
		x += "there are only a finite number of possibilities. Thus the fundamental theorem yields";
		x += "an obvious, but terribly inefficient, finite search technique. By expanding upon the";
		x += "technique of proof as well as the statement of the fundamental theorem, the efficient";
		x += "simplex procedure is derived.";
		x += "It should be noted that the proof of the fundamental theorem given above is of";
		x += "a simple algebraic character. In the next section the geometric interpretation of this";
		x += "theorem is explored in terms of the general theory of convex sets. Although the";
		x += "geometric interpretation is aesthetically pleasing and theoretically important, the";
		x += "reader should bear in mind, lest one be diverted by the somewhat more advanced";
		x += "arguments employed, the underlying elementary level of the fundamental theorem.";
		
		int [] percent = new int [26];
		
		char [] values = x.toCharArray();
		
		for(int i = 0; i < values.length; i++){
			for(int j = 0; j < alfabet.length; j++){
				if(values[i] == alfabet[j]){
					percent[j]++;
					break;
				}
			}
		}
		
		String output = "percent = {";
		int total = 0;
		for(int i = 0; i < percent.length; i++){
			output += percent[i] + ", ";
			total += percent[i];
		}
		System.out.println(output + "}; " + total);

		for(int i = 0; i < percent.length; i++){
			String out = alfabet[i] + " (" + (percent[i]/10) + "): ";
			for(int j = 0; j < percent[i]; j +=10)
				out += "x";
			System.out.println(out);
			out = "";
		}
		
		
//		return alfabet[rand.nextInt(alfabet.length)];
	}

	public static void pickLetter3(){
		String x = "";
		for(int i = 0; i < 2000; i++){
			int pick = rand.nextInt(total);
			int group = 0;
			for(int j = 0; j < alfabet.length; j++){
				group += percent[j];
				if(pick < group){ x += alfabet[j]; break; }
			}
		}
		
		int [] percent = new int [26];
		
		char [] values = x.toCharArray();
		
		for(int i = 0; i < values.length; i++){
			for(int j = 0; j < alfabet.length; j++){
				if(values[i] == alfabet[j]){
					percent[j]++;
					break;
				}
			}
		}
		
		for(int i = 0; i < percent.length; i++){
			String out = alfabet[i] + ": ";
			for(int j = 0; j < percent[i]; j +=10)
				out += "x";
			System.out.println(out);
			out = "";
		}
		
		System.out.println(values.length);
		
//		return alfabet[rand.nextInt(alfabet.length)];
	}
}
