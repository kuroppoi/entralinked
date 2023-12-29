<p align="center">
  <img src="https://raw.githubusercontent.com/kuroppoi/entralinked/master/images/icon.png" alt="icon"/>
</p>
<h1 align="center">Entralinked</h1>
<p align="center">
  <a href="https://github.com/kuroppoi/entralinked/actions"><img src="https://github.com/kuroppoi/entralinked/actions/workflows/build.yml/badge.svg" alt="build"/></a>
  <a href="https://github.com/kuroppoi/entralinked/releases/latest"><img src="https://img.shields.io/github/v/release/kuroppoi/entralinked?labelColor=30373D&label=Release&logoColor=959DA5&logo=github" alt="release"/></a>
</p>

Entralinked is a standalone Game Sync emulator developed for use with Pokémon Black & White and its sequels.\
Its purpose is to serve as a simple utility for downloading Pokémon, Items, C-Gear skins, Pokédex skins, Musicals\
and, in Black 2 & White 2 only, Join Avenue visitors to your game without needing to edit your save file.\
It can also be used to Memory Link with a Black or White save file if you don't have a second DS system.

![preview](https://raw.githubusercontent.com/kuroppoi/entralinked/master/images/preview.gif)

For users: [Quick Setup Guide](https://github.com/kuroppoi/entralinked/wiki/Setup)

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
After tucking in a Pokémon, navigate to http://localhost/dashboard/profile.html to configure Game Sync settings.
