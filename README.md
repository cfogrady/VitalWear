# VitalWear
VitalWear is an engine reimplementation (https://en.wikipedia.org/wiki/Game_engine_recreation)
of Bandai's Vital Bracelet BE for Wear OS. It will eventually also run unique gameplay using
DIM and BE Memory data.

This project cannot be run without having access to a Vital Bracelet BE and the cards with which the
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
file.

**NOTE:** At this time only BE Memories are supported. Loading a DIM may cause the app to malfunction.

From the character selection screen click "Import Card". Once the card image is selected, the user
will be able to set a card name, and specify whether the card has unique sprites. If this flag is
off, Vital Wear will attempt to save space by re-using sprites across cards for a character when the
splash screen sprite of the character is identical.

Clicking "continue", will take the user to a screen asking the user to connect to their bracelet.
On a Vital Bracelet BE, go to "Connect"->"App Loglink". Once the bracelet is ready, tap the bracelet
to the phone. The phone will send a code to the bracelet asking for card verification and after a
few seconds the bracelet will show the insert card icon.

**NOTE:** At this time there must be an active character (from any card) on the Vital Bracelet BE
during validation.

Insert the physical card into the bracelet that matches the card image being imported. Once the BE
has loaded the card, tap the bracelet back to the phone.

From the watch character selection screen, click "New...", then click "Import Card". At this point,
the phone and watch should connect and both should show loading screens. Once the card is imported,
you can select it and start a character from that card.

If using custom card images, you only have to verify the card id against the Vital Bracelet BE once.
Subsequent card images with the same card id will skip straight to connecting with the watch after
the card image is selected on the phone.

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
