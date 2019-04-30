package utils;

public class ClockedValue {
    int clock;
    int value;

    public ClockedValue () {

    }

    public ClockedValue(int clock, int value) {
        this.clock = clock;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getClock() {
        return clock;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ClockedValue{" +
                "clock=" + clock +
                ", value=" + value +
                '}';
    }
}
