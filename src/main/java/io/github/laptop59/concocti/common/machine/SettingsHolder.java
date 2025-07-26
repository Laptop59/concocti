package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.client.gui.components.MachineSettings;

public interface SettingsHolder {
    /**
     * Get the machine settings of this holder.
     * @return the machine settings associated with this holder.
     */
    MachineSettings getMachineSettings();

    /**
     * Negates whether eject is on and returns the new value.
     */
    boolean changeEjectOn();

    /**
     * Negates whether pull is on and returns the new value.
     */
    boolean changePullOn();
}
