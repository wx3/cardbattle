package com.wx3.cardbattle.game.gameevents;

import com.wx3.cardbattle.game.EntityStats;

/**
 * A Buff Recalculation is the phase where entities have their
 * {@link EntityStats} recalculated to reflect any buffs. It 
 * should not be instanced-- it exists so Buff rules can use its
 * class as a trigger.
 * 
 * @author Kevin
 *
 */
public class BuffRecalc extends GameEvent {}
