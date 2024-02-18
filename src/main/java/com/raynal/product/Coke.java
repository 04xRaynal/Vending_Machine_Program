package com.raynal.product;

public class Coke {
	private int quantity;
	private float price;
	
	public Coke(final int quantity, final float price) {
		this.quantity = quantity;
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public float getPrice() {
		return price;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}
	
	@Override
	public String toString() {
		return "Coke [quantity=" + quantity + ", price=" + price + "]";
	}
}
