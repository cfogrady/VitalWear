# VitalWear
VitalWear is an engine reimplementation (https://en.wikipedia.org/wiki/Game_engine_recreation)
of Bandai's Vital Bracelet BE for Wear OS. It will eventually also run unique gameplay using
DIM and BE Memory data.

This project cannot be run without having access to a Vital Bracelet and the cards with which the
user intends to play. This project is meant to facilitate enhanced gameplay for legitimate customers
of Bandai's Vital Bracelet and DIM/BE Memory cards. For this reason, all new card images imported
into the app require validation against Bandai's Vital Bracelet and the physical card.

# Users
## Installation
This program requires the WearOS apk to be install on the user's watch and the companion apk to be
installed on the users phone.

## Setup
When the program is first started, the user will be asked to load the firmware from their phone.
Using the phone app, click the "Import Firmware" button. Then navigate to a copy of the official
1.0.B firmware.

**NOTE:** Use of any other firmware at this time will likely cause the app to malfunction. If this
occurs, clear all data from the app in settings.

Once the firmware is selected the user should see both the watch and phone display loading screens.
After about 30 seconds, the user will be presented with the character selection screen on their
watch.

## Importing New Card Images
From the phone app, click "Import Card Image". This will allow the user to navigate to a card image
file. Once the card image is selected, the user  will be able to set a card name, and specify
whether the card has unique sprites. If this flag is  off, Vital Wear will attempt to save space by
re-using sprites across cards for a character when the splash screen sprite of the character is
identical.

Clicking "continue", will take the user to a screen asking the user to connect to their bracelet.
On a Vital Bracelet, go to "Connect"->"App Loglink". Once the bracelet is ready, tap the bracelet
to the phone. The phone will send a code to the bracelet asking for card verification and after a
few seconds the bracelet will show the insert card icon.

**NOTE:** At this time there must be an active character (from any card) on the Vital Bracelet
during validation.

Insert the physical card into the bracelet that matches the card image being imported. Once the VB
has loaded the card, tap the bracelet back to the phone. The phone will show a loading screen. A few
seconds after the phone is finished loading, a notification should appear on the watch letting the
user know that the card is ready to use.

If using custom card images, you only have to verify the card id against the Vital Bracelet once.
Subsequent card images with the same card id will skip straight to connecting with the watch after
the card image is selected on the phone.

## Character Settings
There are several settings when starting a new character
1) **Background Training** - This allows training to occur in the background of the watch. When this is
   disabled, the watch behaves similar to a vanilla VB and runs a single training mission.
2) **Scale DIM to BEM** - This only shows up for DIM characters, and allows you scale the DIM stats to
   BEM stats and do trainings and battles other BEM characters. When this is enabled the user must
   select a franchise from the other loaded BEM cards. This will determine adventures and battle
   opponents.
3) **Battle Options** - This allows the character to have random battles against only characters
   from the same card (same as physical VB), against any characters from the same franchise, against
   any characters from the same franchise or DIM characters, or against any character from any card
   loaded in the app. 
   1) The latter two settings will also allow the character to do DIM adventure missions or adventure missions from other franchises. Doing adventure missions with these characters will not unlock transformations or gift characters. Those can only be unlocked by completing an adventure with a character originally from the card's franchise.

### DIM Scaling Algorithm
There are minimums, maximums, and averages for all DIM stats across phases built into the program. If a
character's DIM stat is above the average for the next level up it is excluded from the calculation
for maximum.

The averages and standard deviations for Digimon BE Memories released as of 2024-01-30 are also
stored.

If a character shares the same splash sprite as an imported BEM character (and the character doesn't
have unique sprites enabled), those stats are used.

When there is no BEM version of a character and it is upscaled, each of their stats is marked as a
percentage of the range for their level (or the next level if that stat was an outlier), then that
percentage is used to for the BEM stat range, defined as the average plus or minus one standard
deviation. This should keep each characters relative strength to other DIM characters in tact for
BEM stats; however, this also means that most DIM characters won't have outliers far from the
average. There are a few BEM characters with wildly different from the rest which would otherwise
skew stats for the entire clump of weakest or strongest DIM characters for a phase.

## Permissions
At the moment permissions must be manually enabled by going to permissions in App Info on the watch.

## Videos:
https://www.youtube.com/channel/UCnfuVAlPwwKOLTkVx7bW-6Q

## Issues
If there are any issues, please see if there is already a matching issue on github. If not, feel
free to add one.

## Feature Requests
Please add any feature requests as issues on github. This has largely been a single person project,
and I prioritize the gameplay I want for myself, but I'm very open to enhancing gameplay for
everybody.

# Development
To contribute, please fork the project and send pull requests.

## Setup
1) VB-DIM-Reader (https://github.com/cfogrady/VB-DIM-Reader) java8 branch published to maven local using `gradlew publishToMavenLocal`
2) Import project into Android Studio

# Legal
Preface: I am not a lawyer, this is just based on my interpretation of local laws to show that
everything is done in good faith. If you believe I am infringing on any of your rights please open
an issue and I will seek to rectify it as soon as possible.

I am in no way affiliated with Bandai nor anyone else who own any trademarks that may
appear here. Any display of trademarks here is purely to indicate the purpose of this project and
in accordance with good business practice, as permitted by relevant laws.

There are no copyrighted works distributed here, nor works derived from copyrighted works, except
works with a free and open source license permitting distribution. I have no incentive or intention
to do anything that would infringe on anyone's copyrights, trademarks or other rights.
