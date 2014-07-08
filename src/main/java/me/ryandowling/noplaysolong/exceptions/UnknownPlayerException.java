/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package me.ryandowling.noplaysolong.exceptions;

public class UnknownPlayerException extends Exception {
    private static final long serialVersionUID = -5987543214085051018L;

    public UnknownPlayerException(String username) {
        super("Unknown player with username of " + username);
    }
}
