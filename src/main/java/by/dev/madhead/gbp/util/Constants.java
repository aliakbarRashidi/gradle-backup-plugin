/*
 * Copyright 2015 madhead <siarhei.krukau@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package by.dev.madhead.gbp.util;

/**
 * Plugin related constants live here.
 */
public interface Constants {
	/**
	 * This ANSI escape code resets all attributes.
	 */
	String ANSI_RESET_CODE = "\u001B[0m";

	/**
	 * This ANSI escape code is used to set "highlighted" foreground color.
	 */
	String ANSI_HIHGLIGHT_CODE = "\u001B[33m";
}
