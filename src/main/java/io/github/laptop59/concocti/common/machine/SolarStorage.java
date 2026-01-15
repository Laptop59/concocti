package io.github.laptop59.concocti.common.machine;

/** An interface to allow extraction & insertion of 'solar' into entity blocks. Mainly used by the Concocti Solar Collector. */
public interface SolarStorage {
    /**
     * Adds solar to the storage. Returns the amount of solar that was accepted.
     *
     * @param toReceive The amount of solar being received.
     * @param simulate  If true, the insertion will only be simulated, meaning {@link #getSolarStored()} will not change.
     * @return Amount of solar that was (or would have been, if simulated) accepted by the storage.
     */
    long receiveSolar(long toReceive, boolean simulate);

    /**
     * Removes solar from the storage. Returns the amount of solar that was removed.
     *
     * @param toExtract The amount of solar being extracted.
     * @param simulate  If true, the extraction will only be simulated, meaning {@link #getSolarStored()} will not change.
     * @return Amount of solar that was (or would have been, if simulated) extracted from the storage.
     */
    long extractSolar(long toExtract, boolean simulate);

    /**
     * Returns the amount of solar currently stored.
     */
    default long getSolarStored() {
        return 0;
    }

    /**
     * Returns the maximum amount of solar that can be stored.
     */
    long getMaxSolarStored();

    /**
     * Returns if this storage can have solar extracted.
     * If this is false, then any calls to extractSolar will return 0.
     */
    boolean canExtract();

    /**
     * Used to determine if this storage can receive solar.
     * If this is false, then any calls to receiveSolar will return 0.
     */
    boolean canReceive();
}
