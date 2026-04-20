package me.alpha432.oxevy.util.inventory.strategy;

import me.alpha432.oxevy.util.inventory.Result;

public interface SwapStrategy {
    boolean swap(Result result);

    boolean swapBack(int last, Result result);
}
