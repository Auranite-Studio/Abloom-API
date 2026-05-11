# Changelog

All notable changes to the Abloom API project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).



# *1.0.0-beta5*

## Added

* **New Element Type**: Added ETHER element (`ether\_dmg`) with color #24B3A7

  * Ether resonance explosion effect
  * Corruption mob effect: periodic damage and reduces all elemental resistances by 20% per level
  * Duration: 8 seconds (160 ticks)

### Changed

* Updated total element count to 11 types
* **Updated Mob Resistances**: Updated resistance data for multiple mobs across various elements
  * FIRE: Moved Husk and Camel Husk from Weak to Resistant; removed Camel Husk from Weak list
  * ICE: Removed Sulfur Cube from Weak list
  * NATURAL: Significantly updated immune and weak lists - removed Zombie, Zombie Villager, Drowned, Skeleton, Spider, Cave Spider, Witch, Hoglin, Piglin, Piglin Brute from Immune; added Zoglin, Stray, Zombified Piglin, Zombie, Zombie Villager, Zombie Nautilus, Skeleton to Weak list

### Fixed

* Corrected mob resistance entries to match actual tag definitions

### Deprecated

* *(Nothing deprecated in this release)*

### Removed

* *(Nothing removed in this release)*

