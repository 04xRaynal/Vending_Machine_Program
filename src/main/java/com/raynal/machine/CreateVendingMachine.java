package com.raynal.machine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import com.raynal.product.Coke;
import com.raynal.product.Pepsi;
import com.raynal.product.Sprite;

public class CreateVendingMachine {
	private static final List<Float> accepted_coins = Arrays.asList(1.0F, 5.0F, 10.0F, 25.0F, 50.0F, 100.0F);
	
	private static final Map<Integer, Integer> userChosenProductMap = new HashMap<>();
	
	public void setupAndStartMachine() {
		//initial inventory - 10xCoke each 25p, 15xPepsi each 35p, 12xSprite each 45p
		final Coke coke = new Coke(10, 25.0F);
		final Pepsi pepsi = new Pepsi(15, 35.0F);
		final Sprite sprite = new Sprite(12, 45.0F);
		
		startMachine(coke, pepsi, sprite);
	}
	
	public void startMachine(final Coke coke, final Pepsi pepsi, final Sprite sprite) {
		try(final Scanner scanner = new Scanner(System.in)) {
			chooseUser(scanner, coke, pepsi, sprite);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("Error occured while reading/performing user operation. Please try again.");
		}
	}
	
	private void chooseUser(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite) {
		System.out.println("Please enter the appropriate option 1 or 2: \n" + 
							"1. Buy products \n" + 
							"2. Supplier view");
		
		try {
			if(scanner.hasNextInt()) {
				final Integer userInput = scanner.nextInt();
				if(userInput.equals(1)) {
					listAvailableProducts(scanner, coke, pepsi, sprite);
					return;
				}
				else if(userInput.equals(2)) {
					chooseSupplierAction(scanner, coke, pepsi, sprite);
					return;
				}
				
				System.out.println("Please enter either 1 or 2.");
			}
			else {
				System.out.println("A numerical value is expected! Please try again.");
			}
		}
		catch(IllegalStateException ex) {
			System.out.println("Error while reading input! Please try again.");
		}
		
		scanner.nextLine();
		chooseUser(scanner, coke, pepsi, sprite);
	}
	
	private void listAvailableProducts(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite) {
		System.out.println("Please enter the appropriate option to choose a product: \n" + 
							"1. Coke - quantity: " + coke.getQuantity() + " & price: " + coke.getPrice() + "p\n" + 
							"2. Pepsi - quantity: " + pepsi.getQuantity() + " & price: " + pepsi.getPrice() + "p\n" + 
							"3. Sprite - quantity: " + sprite.getQuantity() + " & price: " + sprite.getPrice() + "p");
		try {
			if(scanner.hasNextInt()) {
				final Integer userInput = scanner.nextInt();
				
				if(userInput.equals(1) || userInput.equals(2) || userInput.equals(3)) {
					addProductQuantity(scanner, coke, pepsi, sprite, userInput);
					return;
				}
				else
					System.out.println("Please enter either option 1, 2 or 3.");
			}
		}
		catch(IllegalStateException ex) {
			System.out.println("Error while reading input! Please try again.");
		}
		
		scanner.nextLine();
		listAvailableProducts(scanner, coke, pepsi, sprite);
	}
	
	private void addProductQuantity(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite, final Integer selectedProduct) {
		System.out.println("Enter the quantity required: ");
		
		try {
			if(scanner.hasNextInt()) {
				final Integer userInput = scanner.nextInt();
				
				checkValidQuantity(scanner, coke, pepsi, sprite, selectedProduct, userInput);
				return;
			}
		}
		catch(IllegalStateException ex) {
			System.out.println("Error while reading input! Please try again.");
		}
		
		scanner.nextLine();
		addProductQuantity(scanner, coke, pepsi, sprite, selectedProduct);
	}
	
	private void checkValidQuantity(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite, final Integer selectedProduct, final Integer selectedQuantity) {
		boolean validQuantity = false;
		if(selectedProduct.equals(1))
			validQuantity = selectedQuantity <= coke.getQuantity();
		else if(selectedProduct.equals(2))
			validQuantity = selectedQuantity <= pepsi.getQuantity();
		else if(selectedProduct.equals(3))
			validQuantity = selectedQuantity <= sprite.getQuantity();
		
		if(validQuantity) {
			userChosenProductMap.put(selectedProduct, selectedQuantity);
			System.out.println("Please enter the appropriate option 1 or 2: \n" + 
								"1. Select more products \n" + 
								"2. Pay for the selected products");
			
			try {
				if(scanner.hasNextInt()) {
					final Integer userInput = scanner.nextInt();
					
					if(userInput.equals(1)) {
						listAvailableProducts(scanner, coke, pepsi, sprite);
						return;
					}
					else if(userInput.equals(2)) {
						scanner.nextLine();
						acceptBuyerCoins(scanner, coke, pepsi, sprite);
						return;
					}
					
					System.out.println("Please enter either option 1 or 2.");
				}
			}
			catch(IllegalStateException ex) {
				System.out.println("Error while reading input! Please try again.");
			}
			
			scanner.nextLine();
			checkValidQuantity(scanner, coke, pepsi, sprite, selectedProduct, selectedQuantity);
		}
		else {
			System.out.println("Invalid product quantity, please input the value again.");
			scanner.nextLine();
			addProductQuantity(scanner, coke, pepsi, sprite, selectedProduct);
		}
	}
	
	private void acceptBuyerCoins(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite) {
		final Float payableSum = userChosenProductMap
									.keySet()
									.stream()
									.map(key -> getFinalProductPrice(key, coke, pepsi, sprite))
									.reduce(0.0F, Float::sum);
		
		System.out.println("Final price to be paid: " + payableSum + 
							"\nPlease input coin(s). Accepted coins: " + accepted_coins);
		
		try {
			if(scanner.hasNextLine()) {
				final String enteredCoins = scanner.nextLine();
				final String[] enteredCoinsArray = enteredCoins.split(" ");
				final List<Float> coinsList = Arrays.stream(enteredCoinsArray)
													.map(Float::parseFloat)
													.filter(this::checkIfCoinAccepted)
													.collect(Collectors.toList());
				
				if(coinsList.size() < enteredCoinsArray.length) {
					System.out.println("Invalid coin(s) entered. Please input valid coins.");
					acceptBuyerCoins(scanner, coke, pepsi, sprite);
					return;
				}
				
				final Float enteredCoinsTotal = coinsList.stream()
														.reduce(0.0F, Float::sum);
				if(enteredCoinsTotal < payableSum) {
					askMoreCoins(scanner, coke, pepsi, sprite, payableSum, enteredCoinsTotal);
					return;
				}
				else {
					sendProductsAndRemainder(scanner, coke, pepsi, sprite, payableSum, enteredCoinsTotal);
					return;
				}
			}
		}
		catch(IllegalStateException | PatternSyntaxException ex) {
			System.out.println("Error while reading input! Please try again.");
		}
		
		scanner.nextLine();
		acceptBuyerCoins(scanner, coke, pepsi, sprite);
	}
	
	private Float getFinalProductPrice(final Integer product, final Coke coke, final Pepsi pepsi, final Sprite sprite) {
		if(product.equals(1))
			return userChosenProductMap.get(1) * coke.getPrice();
		else if(product.equals(2))
			return userChosenProductMap.get(2) * pepsi.getPrice();
		else if(product.equals(3))
			return userChosenProductMap.get(3) * sprite.getPrice();
		else
			return 0.0F;
	}
	
	private void askMoreCoins(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite, final Float requiredCoins, final Float paidCoins) {
		System.out.println("Entered coins lower than expected amount. Please add additional amount: " + (requiredCoins - paidCoins) + 
							"\nPlease input coin(s). Accepted coins: " + accepted_coins);
		
		try {	
			if(scanner.hasNextLine()) {
				final String enteredCoins = scanner.nextLine();
				final String[] enteredCoinsArray = enteredCoins.split(" ");
				final List<Float> coinsList = Arrays.stream(enteredCoinsArray)
													.map(Float::parseFloat)
													.filter(this::checkIfCoinAccepted)
													.collect(Collectors.toList());
				
				if(coinsList.size() < enteredCoinsArray.length) {
					System.out.println("Invalid coin(s) entered. Please input valid coins.");
					askMoreCoins(scanner, coke, pepsi, sprite, requiredCoins, paidCoins);
					return;
				}
				
				final Float additionalCoins = coinsList.stream()
														.reduce(0.0F, Float::sum);
				final Float totalCoins = paidCoins + additionalCoins;
				if(totalCoins < requiredCoins) {
					askMoreCoins(scanner, coke, pepsi, sprite, requiredCoins, totalCoins);
					return;
				}
				else {
					sendProductsAndRemainder(scanner, coke, pepsi, sprite, requiredCoins, totalCoins);
					return;
				}
			}
		}
		catch(IllegalStateException | PatternSyntaxException ex) {
			System.out.println("Error while reading input! Please try again.");
		}
		
		scanner.nextLine();
		askMoreCoins(scanner, coke, pepsi, sprite, requiredCoins, paidCoins);
	}
	
	private void sendProductsAndRemainder(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite, final Float requiredCoins, final Float paidCoins) {
		final Integer cokeQuantity = Objects.nonNull(userChosenProductMap.get(1)) ? userChosenProductMap.get(1) : 0;
		final Integer pepsiQuantity = Objects.nonNull(userChosenProductMap.get(2)) ? userChosenProductMap.get(2) : 0;
		final Integer spriteQuantity = Objects.nonNull(userChosenProductMap.get(3)) ? userChosenProductMap.get(3) : 0;
		
		System.out.println("Please take your products: \n" + 
							"Coke: " + cokeQuantity + 
							"\nPepsi: " + pepsiQuantity + 
							"\nSprite: " + spriteQuantity);
		
		final Float overpaidAmount = paidCoins - requiredCoins;
		if(overpaidAmount > 0.0F)
			System.out.println("Also please accept the remaining change: " + overpaidAmount);
		
		return;
	}
	
	private void chooseSupplierAction(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite) {
		System.out.println("Please enter the appropriate option 1 or 2: \n" + 
							"1. View full Inventory \n" + 
							"2. Update stock or prices");
		
		try {
			if(scanner.hasNextInt()) {
				final Integer userInput = scanner.nextInt();
				if(userInput.equals(1)) {
					System.out.println("Full inventory list: \n" + 
										"1. Coke - quantity: " + coke.getQuantity() + " & price: " + coke.getPrice() + "p\n" + 
										"2. Pepsi - quantity: " + pepsi.getQuantity() + " & price: " + pepsi.getPrice() + "p\n" + 
										"3. Sprite - quantity: " + sprite.getQuantity() + " & price: " + sprite.getPrice() + "p");
					
					System.out.println("Supplier action executed successfully... returning to main menu!");
					chooseUser(scanner, coke, pepsi, sprite);
				}
				else if(userInput.equals(2)) {
					updateInventory(scanner, coke, pepsi, sprite);
					return;
				}
				
				System.out.println("Please enter either 1 or 2.");
			}
			else {
				System.out.println("A numerical value is expected! Please try again.");
			}
		}
		catch(IllegalStateException ex) {
			System.out.println("Error while reading input! Please try again.");
		}
		
		scanner.nextLine();
		chooseSupplierAction(scanner, coke, pepsi, sprite);
	}
	
	private void updateInventory(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite) {
		System.out.println("Enter number of the product to update: \n" + 
							"1. Coke \n" + 
							"2. Pepsi \n" + 
							"3. Sprite");
		
		try {
			if(scanner.hasNextInt()) {
				final Integer userInput = scanner.nextInt();
				if(userInput.equals(1) || userInput.equals(2) || userInput.equals(3)) {
					updateProduct(scanner, coke, pepsi, sprite, userInput);
					return;
				}
				
				System.out.println("Please enter either 1, 2 or 3.");
			}
			else {
				System.out.println("A numerical value is expected! Please try again.");
			}
		}
		catch(IllegalStateException ex) {
			System.out.println("Error while reading input! Please try again.");
		}
		
		scanner.nextLine();
		updateInventory(scanner, coke, pepsi, sprite);
	}
	
	private void updateProduct(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite, final Integer productChoice) {
		System.out.println("Please enter the appropriate option 1 or 2: \n" + 
							"1. Update product stock \n" + 
							"2. Update product prices");
		
		try {
			if(scanner.hasNextInt()) {
				final Integer userInput = scanner.nextInt();
				if(userInput.equals(1)) {
					updateQuantity(scanner, coke, pepsi, sprite, productChoice);
					return;
				}
				else if(userInput.equals(2)) {
					updatePrice(scanner, coke, pepsi, sprite, productChoice);
					return;
				}
				
				System.out.println("Please enter either 1 or 2.");
			}
			else {
				System.out.println("A numerical value is expected! Please try again.");
			}
		}
		catch(IllegalStateException ex) {
			System.out.println("Error while reading input! Please try again.");
		}
		
		scanner.nextLine();
		updateProduct(scanner, coke, pepsi, sprite, productChoice);
	}
	
	private void updateQuantity(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite, final Integer productChoice) {
		System.out.println("Please enter the quanity: ");

		try {
			if(scanner.hasNextInt()) {
				final Integer userInput = scanner.nextInt();
				if(userInput < 0) {
					System.out.println("Only a positive number is accepted. Please try again.");
				}
				else {
					if(productChoice.equals(1))
						coke.setQuantity(userInput);
					else if(productChoice.equals(2))
						pepsi.setQuantity(userInput);
					else
						sprite.setQuantity(userInput);
					
					System.out.println("Supplier action executed successfully... returning to main menu!");
					chooseUser(scanner, coke, pepsi, sprite);
				}
			}
			else {
				System.out.println("A numerical value is expected! Please try again.");
			}
		}
		catch (IllegalStateException ex) {
			System.out.println("Error while reading input! Please try again.");
		}

		scanner.nextLine();
		updateQuantity(scanner, coke, pepsi, sprite, productChoice);
	}
	
	private void updatePrice(final Scanner scanner, final Coke coke, final Pepsi pepsi, final Sprite sprite, final Integer productChoice) {
		System.out.println("Please enter the price: ");

		try {
			if(scanner.hasNextFloat()) {
				final Float userInput = scanner.nextFloat();
				if(userInput < 1.0F) {
					System.out.println("A float value below 1 is not accepted. Please try again.");
				}
				else {
					if(productChoice.equals(1))
						coke.setPrice(userInput);
					else if(productChoice.equals(2))
						pepsi.setPrice(userInput);
					else
						sprite.setPrice(userInput);
					
					System.out.println("Supplier action executed successfully... returning to main menu!");
					chooseUser(scanner, coke, pepsi, sprite);
				}
			}
			else {
				System.out.println("A float value is expected! Please try again.");
			}
		}
		catch (IllegalStateException ex) {
			System.out.println("Error while reading input! Please try again.");
		}

		scanner.nextLine();
		updatePrice(scanner, coke, pepsi, sprite, productChoice);
	}
	
	private boolean checkIfCoinAccepted(final Float coin) {
		return accepted_coins.contains(coin);
	}
}
