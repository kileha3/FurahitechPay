package com.furahitechpay.util.executors;

import android.os.Handler;
import android.os.Looper;

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


public class ThreadManager {
	
	private static ThreadManager threadManager;
	
	private Handler handler;
	
	private ThreadManager() {
		handler = new Handler(Looper.getMainLooper());
	}
	
	public static synchronized ThreadManager getInstance() {
		if (threadManager == null) {
			threadManager = new ThreadManager();
		}
		return threadManager;
	}
	
	public void post(Runnable runnable) {
		handler.post(runnable);
	}
	
}