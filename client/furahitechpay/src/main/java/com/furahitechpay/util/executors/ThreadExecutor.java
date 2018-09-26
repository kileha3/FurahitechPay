package com.furahitechpay.util.executors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * Copyright (c) 2018 Lukundo Kileha
 *
 * Licensed under The MIT License,
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 */


public class ThreadExecutor {
	
	private static final int CORE_POOL_SIZE = 3;
	
	private static final int MAX_POOL_SIZE = 5;
	
	private static final int KEEP_ALIVE_TIME = 120;
	
	private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
	
	private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<>();
	
	
	private static volatile ThreadExecutor threadExecutor;
	
	private ThreadPoolExecutor threadPoolExecutor;
	
	private ThreadExecutor() {
		threadPoolExecutor = new ThreadPoolExecutor(
				CORE_POOL_SIZE,
				MAX_POOL_SIZE,
				KEEP_ALIVE_TIME,
				TIME_UNIT,
				WORK_QUEUE);
	}
	
	public static synchronized ThreadExecutor getInstance() {
		if (threadExecutor == null) {
			threadExecutor = new ThreadExecutor();
		}
		
		return threadExecutor;
	}
	
	public Future<?> execute(Runnable runnable) {
		return threadPoolExecutor.submit(runnable);
	}
}