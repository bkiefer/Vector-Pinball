# Vector Pinball

This is an attempt to port this game to Linux using Swing and the OpenGL renderer. 

I tried to keep the changes to the minimum and replaced the missing function to the best of my knowledge.
Unfortunately, the GL rendering does not work and i don't know enough about it to understand what's going wrong.
Once that is fixed, there is more to do to get it closer to the original, but that has to be done first.

The current version is on the developer branch and can be build by executing

mvn install

in the app directory of the project. A run.sh script is provided to start the app. Close the window to exit, the menu item does not work properly and the app continues to run in the background although the window is gone.

# **** ORIGINAL README ****

Vector Pinball is a pinball game for Android devices.
It is released under version 3 of the GPL; see [COPYING](COPYING.txt) for the license text.

The graphics are deliberately simple; currently everything is drawn with lines and circles.
The focus is on gameplay and accurate physics.
It uses the libgdx Java wrapper for the Box2D physics engine.
Thanks to Peter Drescher for the sound effects; see [his article on creating them](https://www.twittering.com/webarchive_articles/FMOD%20for%20Android%20-%20O'Reilly%20Broadcast.html).

The GitHub project page is: [github.com/dozingcat/Vector-Pinball/](https://github.com/dozingcat/Vector-Pinball/).
See [devnotes.txt](devnotes.txt) for an overview of the code layout.

There is a very experimental table editor at [github.com/dozingcat/Vector-Pinball-Editor/](https://github.com/dozingcat/Vector-Pinball-Editor/)

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

Vector Pinball includes [the libgdx library by Bad Logic Games](http://libgdx.badlogicgames.com/).
libgdx is used under [the terms of Version 2.0 of the Apache License](https://www.apache.org/licenses/LICENSE-2.0).

Sound, music, & audio code by [pdx of Twittering Machine](http://www.twittering.com).

# How to Translate

Make a new folder in "app/src/main/res" named "values-**_lang_**" (with only at least 2 letters), copy-paste the strings.xml file from the "values" folder into it, and start editing it to the language of your choice.

Also make a folder in "fastlane/metadata/android" with a folder named "**_lang_**" (with only at least 2 letters), and copy the .txt files from "en_US", and simply overwrite them with the description in your own language. The "title.txt" file is entirely optional if you feel like the title could also be translated.

## Download

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="100">]([https://f-droid.org/packages/com.dozingcatsoftware.bouncy/])
[<img src="https://user-images.githubusercontent.com/33793273/132640445-ee1c74c2-9330-4ba9-93f8-218acd52fab9.png">](https://play.google.com/store/apps/details?id=com.dozingcatsoftware.bouncy)
