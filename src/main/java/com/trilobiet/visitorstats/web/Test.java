package com.trilobiet.visitorstats.web;

/**
 * Multiple interface inheritance in Java
 * @author acdhirr
 */
interface I1 {
    
	int foo();
    
    default int getNumber() {
    	return 1;
    }
}

interface I2 {

	int foo();

    default int getNumber() {
    	return 2;
    }

    default int getAnotherNumber() {
    	return 3;
    }

}

public class Test implements I1, I2 {

	@Override
	public int foo() {

		return 0;
	}

	@Override
	public int getNumber() {

		return I1.super.getNumber();
	}
}


