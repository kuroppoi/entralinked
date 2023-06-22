# Entralinked
[![build](https://github.com/kuroppoi/entralinked/actions/workflows/dist-upload-artifact.yml/badge.svg)](https://github.com/kuroppoi/entralinked/actions)

Entralinked is a standalone Game Sync emulator developed for use with Pokémon Black & White and Pokémon Black 2 & White 2.\
Its purpose is to serve as a simple utility for downloading Pokémon, Items, C-Gear skins, Pokédex skins and Musicals\
without needing to edit your save file.

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
Entralinked has a built-in DNS server. In order for your game to connect, you must configure the DNS settings of your DS.\
By default, Entralinked is configured to use the local host of the system.\
After tucking in a Pokémon, navigate to `http://localhost/dashboard/profile.html` in a web browser to configure Game Sync settings.
