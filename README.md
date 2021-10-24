pokernow.club Handhistory Converter
===================================

Features
--------
* Supports all game types Pot limit Omaha High, Pot limit Omaha Hi/lo and no limit Texas Hold'em.
* Provides Hold'em Manager Micro Stakes support by configurable currencyFactor amount.
* Using a folder with converted File(s) as Pocker Tracker Auto-Import folder could enable the HUD in your browser.  
* Converts pokernow.club csv files to Pokerstars hand history file text format

### Known Limitations
* Only cash games and no tournaments are yet supported
* Player names containing parantheses '(' or ')' are not supported without mapping in name-mappings.properties

Quick Start
-----------
You need a more or less recent Java SE Runtime Environment to run the released program versions. Version 8+ should work.

For a quick start you can unzip the release version (without subdirectory, but directly all three files) in a folder where you've already got one or more csv's from pokernow.club.

Execute from the directory with containing 'handhistory-converter-0.2.2-jar-with-dependencies.jar' and the two properties files:

```
java -jar handhistory-converter-0.2.2-jar-with-dependencies.jar
```

The csv files from pokernow.club in the directory should then get converted.

### Example Usage with PockerTracker4

Downloading a recent file from pokernow.club and converting it can enable the HUD.
You would just set the auto import folder of PockerTracker4 to a directory with the latest converted file.
In Hold'em Manager the HUD activation was not possible. It is restrictive about expected programs and the type of hand history files

![PokernowHUD](https://github.com/evolutionsoftswiss/pokernow-handhistory-converter/blob/develop/PokernowHud.png)

### Configuring Pokernow Handhistory Converter
Use conversion.properties and name-mappings.properties to configure the conversion program.
There are two properties configuration files to configure the behavior of the converter program.

In conversion.properties you can only adapt the right side of the properties after the equals '=' sign. In name-mappings.properties you define both sides, a key on the left with several values on the right.

#### Game Types
Use the three game types in conversion.properties to specify what game types you want to convert.
The following configuration converts only Texas hands and skips present hands of the other Omaha game types:

```
convertOmahaHiHands = false
convertOmahaHiLoHands = false
convertTexasHands = true
```

#### Convert your hole cards
If you want to parse your hole cards, you need to adapt both property files
In conversion.properties set

```
readYourHoleCards = true
yourUniqueName = yourUniqueAlias
```

In pokernow logs the hole cards shown belong to the player who downloaded the history file. With the file itself later the information to which present player those hole cards belong is missing.

Additionally to support your multiple nicknames you need to adapt also the name-mappings.properties for the hole card conversion to work.

The right side of yourUniqueName in conversion.properties has to match the left side in the corresponding name-mappings.properties file. The example uses 'yourUniqueAlias'.

```
yourUniqueAlias = pokernowNickname0,pokernowNickname10
```

#### Other conversion.properties

The remaining properties in *conversion.properties* define a *currencyReductionFactor* and two folders. The *currencyReductionFactor* reduces all amounts by the value on the right. If you're playing a 1$/2$ game and use ten as *currencyReductionFactor* the blinds become 0.1$/0.2$ in the converted hands. This allows you to use e.g. the small stakes Hold'em Manager to import the converted hands even if you played with higher blinds at pokernow.club.

You can set the source folder of PokerNow hand histories with *folderWithLatestCsv*. *folderOfConvertedCsv* is the directory where the converted files are written. The example below shows a Windows file path notation for C:\Users\username\...

```
currencyReductionFactor = 10
folderWithLatestCsv = \\Users\\username\\PokerNow\\HandHistoryCsvs
folderOfConvertedCsv = \\Users\\username\\PokerNow\\converted
```

#### Map poker buddy names with name-mappings.properties

The nicknames on pokernow.club for you and other players can change often.

The pokernow hand history converter provides name-mappings.properties to handle all nicknames.
You have to map at least nicknames containing parantheses '(' or ')' to a name on the left preferably without special characters.
The following lines show *yourUniqueName* and two other lines for your buddy names mapping. On the right side are all known and occuring nicknames present in the csv's. They got mapped to the single names on the left.

The converted hand history file contains then the player names yourUniqueName, buddyName1 and buddyName2. Those names will also be imported from PockerTracker or Hold'em Manager and used there

```
yourUniqueName = pokernowNickname0,pokernowNickname10
buddyName1 = pokernow(parantheses)Nickname1
buddyName2 = pokernowNickname22,pokernowNickname44
```