package com.example.vendingmachine.model;

import com.example.vendingmachine.exeption.NotSufficientChangeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.example.vendingmachine.model.Input.Money.DIME;
import static com.example.vendingmachine.model.Input.Money.DOLLAR;
import static com.example.vendingmachine.model.Input.Money.NICKEL;
import static com.example.vendingmachine.model.Input.Money.QUARTER;
import static com.example.vendingmachine.model.VendingMachine.State.ADDING_MONEY;
import static com.example.vendingmachine.model.VendingMachine.State.DISPERSING;
import static com.example.vendingmachine.model.VendingMachine.State.GIVING_CHANGE;
import static com.example.vendingmachine.model.VendingMachine.State.RESTING;
import static com.example.vendingmachine.model.VendingMachine.State.TERMINAL;

enum Category {
    MONEY(Input.Money.class),
    ITEM_SELECTION(Input.Item.class),
    QUIT_TRANSACTION(Input.Abort.class),
    SHUT_DOWN(Input.Stop.class);

    private Input[] values;

    Category(Class<? extends Input> kind) {
        this.values = kind.getEnumConstants();
    }

    private static Map<Input, Category> categories = new HashMap<>();

    static {
        for (Category c : Category.class.getEnumConstants())
            for (Input input : c.values)
                categories.put(input, c);
    }

    public static Category categorize(Input input) {
        return categories.get(input);
    }
}

interface Command {
    default void next(Input input) {
        throw new RuntimeException("Only call next(Input input) for non-transient states");
    }

    default void next() {
        throw new RuntimeException("Only call next() for StateDuration.TRANSIENT states");
    }

    default void output(int amount) {
        System.out.println(amount);
    }
}

public class VendingMachine {
    private State state = RESTING;
    private int amount = 0;
    private Input selection = null;
    private EnumMap<State, Command> em = new EnumMap<>(State.class);
    private Inventory<Input.Money> cashInventory = new Inventory<>();
    private Inventory<Input.Item> itemInventory = new Inventory<>();

    public VendingMachine() {
        initialize();
    }

    private void initialize() {

        for (Input.Money input : Input.Money.class.getEnumConstants()) {
            cashInventory.put(input, 5);
        }
        for (Input.Item input : Input.Item.class.getEnumConstants()) {
            itemInventory.put(input, 5);
        }

        em.put(RESTING, new Command() {
            @Override
            public void next(Input input) {
                switch (Category.categorize(input)) {
                    case MONEY:
                        amount += input.amount();
                        cashInventory.add((Input.Money) input);
                        state = ADDING_MONEY;
                        break;
                    case SHUT_DOWN:
                        state = TERMINAL;
                    default:
                }
            }
        });
        em.put(ADDING_MONEY, new Command() {
            @Override
            public void next(Input input) {
                switch (Category.categorize(input)) {
                    case MONEY:
                        amount += input.amount();
                        cashInventory.add((Input.Money) input);
                        break;
                    case ITEM_SELECTION:
                        selection = input;
                        if (amount < selection.amount())
                            System.out.println("Insufficient money for " + selection);
                        else if (!itemInventory.hasItem((Input.Item) input))
                            System.out.println("This item is run out");
                        else {
                            itemInventory.deduct((Input.Item) input);
                            state = DISPERSING;
                        }
                        break;
                    case QUIT_TRANSACTION:
                        state = GIVING_CHANGE;
                        break;
                    case SHUT_DOWN:
                        state = TERMINAL;
                    default:
                }
            }
        });
        em.put(DISPERSING, new Command() {
            @Override
            public void next() {
                System.out.println("Here is your " + selection);
                amount -= selection.amount();
                state = GIVING_CHANGE;
            }
        });
        em.put(GIVING_CHANGE, new Command() {
            @Override
            public void next() {
                if (amount > 0) {
                    List<Input.Money> changes = new ArrayList<>();
                    while (amount > 0)
                        if (amount >= DOLLAR.getValue() && cashInventory.hasItem(DOLLAR)) {
                            changes.add(DOLLAR);
                            cashInventory.deduct(DOLLAR);
                            amount -= 100;
                        } else if (amount >= QUARTER.getValue() && cashInventory.hasItem(QUARTER)) {
                            changes.add(QUARTER);
                            cashInventory.deduct(QUARTER);
                            amount -= 25;
                        }else if (amount >= DIME.getValue() && cashInventory.hasItem(DIME)) {
                            changes.add(DIME);
                            cashInventory.deduct(DIME);
                            amount -= 10;
                        }else if (amount >= NICKEL.getValue() && cashInventory.hasItem(NICKEL)) {
                            changes.add(NICKEL);
                            cashInventory.deduct(NICKEL);
                            amount -= 5;
                        }
                        else
                            throw  new NotSufficientChangeException("Not sufficient change, please try another product");

                    System.out.println("Your change: " + changes.toString());
                }
                state = RESTING;
            }
        });
        em.put(TERMINAL, new Command() {
            @Override
            public void output(int amount) {
                System.out.println("Halted!");
            }
        });
    }

    enum StateDuration {
        TRANSIENT
    }

    enum State {
        RESTING,
        ADDING_MONEY,
        DISPERSING(StateDuration.TRANSIENT),
        GIVING_CHANGE(StateDuration.TRANSIENT),
        TERMINAL;

        private boolean isTransient = false;

        State() {
        }

        State(StateDuration trans) {
            this.isTransient = true;
        }
    }

    public Inventory<Input.Money> getCashInventory() {
        return cashInventory;
    }

    public Inventory<Input.Item> getItemInventory() {
        return itemInventory;
    }

    public void run(Supplier<Input> gen) {
        while (state != TERMINAL) {
            Command command = em.get(state);
            command.next(gen.get());
            while (state.isTransient) {
                command = em.get(state);
                command.next();
            }
            command = em.get(state);
            command.output(amount);
        }
    }
}

class RandomInputGenerator implements Generator<Input> {
    @Override
    public Input next() {
        return Input.randomSelection();
    }
}

class RandomInputSupplier implements Supplier<Input> {
    @Override
    public Input get() {
        return Input.randomSelection();
    }
}

// Create Inputs from a file of ';'-separated strings:
class FileInputSupplier implements Supplier<Input> {
    private Iterator<String> input;

    FileInputSupplier(String fileName) {
        try {
            input = Files.lines(Paths.get(fileName))
                    .skip(1) // Skip the comment line
                    .flatMap(s -> Arrays.stream(s.split(";")))
                    .map(String::trim)
                    .collect(Collectors.toList())
                    .iterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Input get() {
        if (!input.hasNext())
            throw new NoSuchElementException("No input!");
        String name = input.next().trim();
        Input result = null;
        if (name.equals("NICKEL") || name.equals("DIME") || name.equals("QUARTER") || name.equals("DOLLAR")) {
            result = Enum.valueOf(Input.Money.class, name);
        } else if (name.equals("TOOTHPASTE") || name.equals("SODA") || name.equals("CHIPS") || name.equals("SOAP"))
            result = Enum.valueOf(Input.Item.class, name);
        else if (name.equals("ABORT_TRANSACTION"))
            result = Enum.valueOf(Input.Abort.class, name);
        else if (name.equals("STOP"))
            result = Enum.valueOf(Input.Stop.class, name);
        return result;
    }
}
