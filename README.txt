#-------------------------------------------------------------------------------
# BBC News Reader
# Released under the BSD License. See README or LICENSE.
# Copyright (c) 2011, 2012, Digital Lizard (Oscar Key, Thomas Boby)
# All rights reserved.
#-------------------------------------------------------------------------------
Authors: Digital Lizard (Oscar Key, Thomas Boby)
License: BSD License <http://www.opensource.org/licenses/bsd-license.php>

BBC News Reader does just that, it reads the BBC rss feeds and retrieves the associated images and places them in a nice gui for browsing on Android based phones.
It has currently been tested on the following Android versions:
	-1.6
	-2.2
	-2.3
	-4.0
However, it should probably work on any version.
The app is fairly screen independent and should scale the amount of news displayed to suit any screen size or orientation.

We use a lightweight Android rss library: android-rss (https://github.com/ahorn/android-rss) to read the news feeds. Feel free fix and/or improve it.
The UI is built around ActionBarSherlock (http://actionbarsherlock.com) (currently v4.1.0).
We also use Matthew Wiggins' SeekBarPreference class (http://android.hlidskialf.com/blog/code/android-seekbar-preference).
Finally, we use the Eula class from the Android sample game "Divide and Conquer"
(http://code.google.com/p/apps-for-android/source/browse/trunk/DivideAndConquer/src/com/google/android/divideandconquer/Eula.java).

Although this app uses BBC content we are in no way affiliated with the BBC. Their content belongs entirely to them. This app simply improves the viewing experience from an Android phone.



Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
Neither the name of the Digital Lizard nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
