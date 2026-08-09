package io.github.mcagents.chat.mods.environment;

import java.util.Objects;

/**
 * Thrown when side-bound code is reached from the wrong physical side.
 *
 * <p>This exception is what a mistake is supposed to look like. Without the
 * guard that throws it, the same mistake surfaces as a
 * {@code NoClassDefFoundError} several frames deeper, at whatever moment the
 * class was first touched, naming a Minecraft class nobody in this project
 * wrote. That is a bug report nobody can act on.</p>
 *
 * <p>Unchecked, because there is no recovery: a caller cannot become the other
 * side. The fix is always at the call site.</p>
 */
public class WrongSideException extends IllegalStateException {

    /**
     * Serialization identity. Fixed so a value serialized by one build
     * deserializes in the next.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The side the code needed. Never {@code null}.
     */
    private final PhysicalSide required;

    /**
     * The side it actually ran on. Never {@code null}.
     */
    private final PhysicalSide actual;

    /**
     * Records a crossing of the side boundary.
     *
     * @param feature What was being reached, named so the message says where to
     *                look.
     * @param required The side that feature belongs to.
     * @param actual The side the call was made from.
     * @throws NullPointerException When either side is {@code null}.
     */
    public WrongSideException(String feature, PhysicalSide required, PhysicalSide actual) {
        super(feature + " is " + Objects.requireNonNull(required, "required cannot be null").code()
                + " only, but was reached on " + Objects.requireNonNull(actual, "actual cannot be null").code()
                + ". This is a wiring mistake: the entry point for a side must only be started on that side.");
        this.required = required;
        this.actual = actual;
    }

    /**
     * Returns the side the code belongs to.
     *
     * @return The required side, never {@code null}.
     */
    public PhysicalSide required() {
        return required;
    }

    /**
     * Returns the side the call was actually made from.
     *
     * @return The actual side, never {@code null}.
     */
    public PhysicalSide actual() {
        return actual;
    }
}
