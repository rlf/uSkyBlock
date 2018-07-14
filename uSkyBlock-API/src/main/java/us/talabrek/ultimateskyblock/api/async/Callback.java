package us.talabrek.ultimateskyblock.api.async;

/**
 * Runnable with state.
 * @since v2.7.4
 */
public abstract class Callback<T> implements Runnable {
    private volatile T state;
    public void setState(T state) {
        this.state = state;
    }
    public T getState() {
        return state;
    }
}
