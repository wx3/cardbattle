/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Kevin Lin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
/**
 * 
 */
package com.wx3.cardbattle.ai;

import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.game.GameInstance;

/**
 * Updates all of the AIs in a single thread.
 * 
 * @author Kevin
 */
public class AIManager {
	
	final static Logger logger = LoggerFactory.getLogger(AIManager.class);

	// Use a concurrent collection so we can safely iterate over the list 
	// while other threads add to it:
	private Collection<SimpleAI> ais = new ConcurrentLinkedQueue<SimpleAI>();
	
	private UpdateTask task;
	private long period;
	
	/**
	 * Update task runs the the update method on the manager
	 * @author Kevin
	 *
	 */
	class UpdateTask extends TimerTask {
		
		private AIManager manager;
		
		public UpdateTask(AIManager manager) {
			this.manager = manager;
		}

		@Override
		public void run() {
			manager.update();
		}
	
	}
	
	public AIManager(int seconds) {
		period = seconds * 1000;
	}
	
	public void start() {
		task = new UpdateTask(this);
		Timer timer = new Timer();
		timer.schedule(task, 0, period);
	}
	
	/**
	 * Add a new AI to be updated by the manager
	 * @param ai
	 */
	public void registerAI(SimpleAI ai){
		ais.add(ai);
	}
	
	void update() {
		long start = System.nanoTime();
		Iterator<SimpleAI> iter = ais.iterator();
		int i = 0;
		while(iter.hasNext()) {
			SimpleAI ai = iter.next();
			if(ai.gameView.gameOver) {
				iter.remove();
			} else {
				try {
					ai.update();	
				} catch (Exception ex) {
					logger.error("AI threw exception, removing: " + ex.getCause());
					iter.remove();
				}
				++i;
			}
		}
		long finish = System.nanoTime();
		double duration = (finish - start) / 1e9;
		logger.info("Updated " + i + " ais in " + duration + " seconds");
	}
}
