package com.kris.fiddle;

import java.util.ArrayList;
import java.util.List;

public class Letter {
	char name;
	Point coordinate;
	List <Letter> neighbors;
	
	public Letter(char letter, Point coordinate){
		this.name = letter;
		this.coordinate = coordinate;
		this.neighbors = new ArrayList<Letter> ();
	}
	
	public void addNeighbor(Letter neighbor){
		neighbors.add(neighbor);
	}
}
