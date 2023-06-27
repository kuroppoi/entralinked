# Entralinked
[![build](https://github.com/kuroppoi/entralinked/actions/workflows/dist-upload-artifact.yml/badge.svg)](https://github.com/kuroppoi/entralinked/actions)

Entralinked is a standalone Game Sync emulator developed for use with Pokémon Black & White and its sequels.\
Its purpose is to serve as a simple utility for downloading Pokémon, Items, C-Gear skins, Pokédex skins and Musicals\
to your game without needing to edit your save file.

## Building

#### Prerequisites

- Java 17 Development Kit

```
git clone --recurse-submodules https://github.com/kuroppoi/entralinked.git
cd entralinked
./gradlew dist
```

## Usage

Execute `entralinked.jar`, or without the user interface:
```
java -jar entralinked.jar disablegui
```
Entralinked has a built-in DNS server.\
In order for your game to connect, you must configure the DNS settings of your DS.\
By default, Entralinked is configured to automatically use the local host of the system.\
This approach is not always accurate, however, and you may need to manually configure it in `config.json`.\
If you receive error code `60000` when trying to connect, erase the Nintendo WFC Configuration of your DS and try again.\
After tucking in a Pokémon, navigate to `http://localhost/dashboard/profile.html` in a web browser to configure Game Sync settings.
