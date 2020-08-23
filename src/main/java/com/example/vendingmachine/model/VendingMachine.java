package com.example.vendingmachine.model;

import com.example.vendingmachine.exeption.NotSufficientChangeException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.vendingmachine.model.Input.Money.*;
import static com.example.vendingmachine.model.VendingMachine.State.*;

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
    default String next(Input input) {
        throw new RuntimeException("Only call next(Input input) for non-transient states");
    }

    default String next() {
        throw new RuntimeException("Only call next() for StateDuration.TRANSIENT states");
    }

    default String output(int amount) {
        return String.valueOf(amount);
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
            public String next(Input input) {
                switch (Category.categorize(input)) {
                    case MONEY:
                        amount += input.amount();
                        cashInventory.add((Input.Money) input);
                        state = ADDING_MONEY;
                        break;
                    case ITEM_SELECTION:
                        return "Please deposit money first, then choose item!";
                    case SHUT_DOWN:
                        state = TERMINAL;
                    default:
                }
                return "Current deposit: " + amount;
            }
        });
        em.put(ADDING_MONEY, new Command() {
            @Override
            public String next(Input input) {
                switch (Category.categorize(input)) {
                    case MONEY:
                        amount += input.amount();
                        cashInventory.add((Input.Money) input);
                        break;
                    case ITEM_SELECTION:
                        selection = input;
                        if (amount < selection.amount())
                            return "Insufficient money for " + selection;
                        else if (!itemInventory.hasItem((Input.Item) input))
                            return "This item is run out";
                        else {
                            itemInventory.deduct((Input.Item) input);
                            state = DISPERSING;
                            return "Here is your: " + selection;
                        }
                    case QUIT_TRANSACTION:
                        state = GIVING_CHANGE;
                        break;
                    case SHUT_DOWN:
                        state = TERMINAL;
                    default:
                }
                return "Current deposit: " + amount;
            }
        });
        em.put(DISPERSING, new Command() {
            @Override
            public String next() {
                amount -= selection.amount();
                state = GIVING_CHANGE;
                return "Here is your " + selection;
            }
        });
        em.put(GIVING_CHANGE, new Command() {
            @Override
            public String next() {
                List<Money> changes = new ArrayList<>();
                if (amount > 0) {
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
                }
                state = RESTING;
                return "Your change: " + changes.toString();
            }
        });
        em.put(TERMINAL, new Command() {
            @Override
            public String output(int amount) {
                return "Halted!";
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

    public String run(Input input){
        String message;
        Command command = em.get(state);
        message = command.next(input);
        return message;
    }
}

