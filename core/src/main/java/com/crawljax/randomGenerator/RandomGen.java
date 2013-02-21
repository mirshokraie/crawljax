package com.crawljax.randomGenerator;

import java.util.Random;

public class RandomGen {

	
	static Random rand;
	private int seed; 
	public RandomGen(){
		seed=10;
		rand=new Random(seed);
	}
	
	public int getNextRandomInt(int n){
		return rand.nextInt(n);
	}
}
