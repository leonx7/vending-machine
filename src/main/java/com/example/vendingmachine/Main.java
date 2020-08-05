package com.example.vendingmachine;

import java.util.function.Supplier;

public class Main {
    public static void main(String[] args){

        VendingMachine vendingMachine = new VendingMachine();
        Supplier<Input> gen = new RandomInputSupplier();
        if (args.length == 1)
            gen = new FileInputSupplier(args[0]);
        vendingMachine.run(gen);
    }
}
