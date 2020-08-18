package com.example.vendingmachine.model;

import com.google.common.collect.ObjectArrays;

import java.util.Random;

public interface Input {
    enum Item implements Input {
        TOOTHPASTE(200),
        CHIPS(75),
        SODA(100),
        SOAP(50);

        private int value;

        Item(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }


        @Override
        public int amount() {
            return value;
        }
    }

    enum Money implements Input {
        NICKEL(5),
        DIME(10),
        QUARTER(25),
        DOLLAR(100);

        private int value;

        Money(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }


        @Override
        public int amount() {
            return value;
        }
    }

    enum Abort implements Input {
        ABORT_TRANSACTION() {
            public int amount() {
                throw new RuntimeException("ABORT.amount()");
            }
        }
    }

    enum Stop implements Input {
        STOP() {
            public int amount() {
                throw new RuntimeException("SHUT_DOWN.amount()");
            }
        }
    }

    default int amount() {
        throw new RuntimeException("No amount for this input");
    }

    Random rand = new Random(47);

    static Input randomSelection() {
        Input[] values = ObjectArrays.concat(Input.Money.values(), Input.Item.values(), Input.class);
        values = ObjectArrays.concat(values, Input.Abort.values(), Input.class);
        return values[rand.nextInt(values.length - 1)];
    }
}
