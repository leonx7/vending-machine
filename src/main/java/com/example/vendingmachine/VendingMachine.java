package com.example.vendingmachine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.example.vendingmachine.VendingMachine.State.DISPERSING;
import static com.example.vendingmachine.VendingMachine.State.TERMINAL;

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
    private State state = State.RESTING;
    private int amount = 0;
    private Input selection = null;
    private EnumMap<State, Command> em = new EnumMap<>(State.class);

    public VendingMachine() {
        initialize();
    }

    public void initialize() {
        em.put(State.RESTING, new Command() {
            @Override
            public void next(Input input) {
                switch (Category.categorize(input)) {
                    case MONEY:
                        amount += input.amount();
                        state = State.ADDING_MONEY;
                        break;
                    case SHUT_DOWN:
                        state = TERMINAL;
                    default:
                }
            }
        });
        em.put(State.ADDING_MONEY, new Command() {
            @Override
            public void next(Input input) {
                switch (Category.categorize(input)) {
                    case MONEY:
                        amount += input.amount();
                        break;
                    case ITEM_SELECTION:
                        selection = input;
                        if (amount < selection.amount())
                            System.out.println("Insufficient money for " + selection);
                        else
                            state = DISPERSING;
                        break;
                    case QUIT_TRANSACTION:
                        state = State.GIVING_CHANGE;
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
                state = State.GIVING_CHANGE;
            }
        });
        em.put(State.GIVING_CHANGE, new Command() {
            @Override
            public void next() {
                if (amount > 0) {
                    System.out.println("Your change: " + amount);
                    amount = 0;
                }
                state = State.RESTING;
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
        if (name.equals("NICKEL")  || name.equals("DIME") || name.equals("QUARTER") || name.equals("DOLLAR")){
            result = Enum.valueOf(Input.Money.class, name);
        }
        else if(name.equals("TOOTHPASTE") || name.equals("SODA") || name.equals("CHIPS") || name.equals("SOAP"))
            result = Enum.valueOf(Input.Item.class, name);
        else if(name.equals("ABORT_TRANSACTION"))
            result = Enum.valueOf(Input.Abort.class, name);
        else if(name.equals("STOP") )
            result = Enum.valueOf(Input.Stop.class, name);
        return result;
    }
}
