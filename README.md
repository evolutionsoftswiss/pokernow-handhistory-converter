Pokernow.club Handhistory Converter
===================================

Features
--------
* Supports all game types Pot limit Omaha High, Pot limit Omaha Hi/lo and no limit Texas Hold'em.
* Provides Hold'em Manager Micro Stakes support by configurable currencyFactor amount.
* Using a folder with converted File(s) as Pocker Tracker Auto-Import folder could enable the HUD in your browser.  

Quick Start
-----------
You need a more or less recent Java SE Runtime Environment to run the released program versions. Version 8+ should work.

For a quick start you can unzip the release version (without subdirectory, but directly all three files) in a folder where you've already got one or more csv's from pokernow.club.

Execute from the directory with containing 'handhistory-converter-0.1.0-jar-with-dependencies.jar' and the two properties files:

```
java -jar handhistory-converter-0.1.0-jar-with-dependencies.jar
```

The csv files from pokernow.club in the directory should then get converted.

Use conversion.properties and name-mappings.properties to configure the conversion program.

More details about configuration in the properties files follow.