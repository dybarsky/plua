
# PLUA cards

[![Blog Post](https://img.shields.io/badge/blog-post-yellow.svg)](http://dybarsky.blogspot.com/2015/03/plua-cards.html)
&nbsp;&nbsp;
[![PlayStore](https://img.shields.io/badge/play-store-blue.svg)](https://play.google.com/store/apps/details?id=dmax.plua)


Offline Android flashcard app for learning vocabulary, **Ukrainian ⇄ Polish**.

A card shows a word. Tap it to reveal the translation, swipe to get the next card. You can add, edit and delete word
pairs, and switch which language is shown first.

Card order is configurable in **Settings** (overflow menu): *random* (default) or *spaced repetition*. In the spaced
mode the translation side of a card shows **Recalled** / **Forgot** buttons; forgotten words are shown earlier, recalled
ones later (see [spaced repetition][1]). The order is applied whenever the list is loaded: after changing the setting or
tapping Reload. The app ships with about 1500
word pairs (nouns, adjectives, verbs) that are imported into a local SQLite database on first launch.

No network, no accounts, no permissions, no analytics. See [PRIVACY.md](PRIVACY.md).

### Build

Java, XML views, single module. Requires JDK 17+ (21 recommended) and Android SDK platform 36.

	./gradlew assembleDebug        # debug APK
	./gradlew testDebugUnitTest    # unit tests
	./gradlew lintDebug            # lint (uses lint-baseline.xml)
	./gradlew bundleRelease        # release AAB

Release signing uses `~/.android/dmax.keystore` and reads `keystorePassword` from `~/.gradle/gradle.properties`;
without it the release bundle is built unsigned. See [AGENTS.md](AGENTS.md) and `docs/` for architecture notes.

===========

### Developed By

Maksym Dybarskyi

===========

### License

	The MIT License (MIT)
	Copyright © 2015 Maxim Dybarsky

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the “Software”), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.

[1]: https://en.wikipedia.org/wiki/Spaced_repetition
