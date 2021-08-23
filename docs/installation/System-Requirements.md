---
parent: Installation and Usage
nav_order: 3
title: System Requirements
---

# System Requirements

When running openrouteservice, by far the biggest consideration that needs to be made is the amount of RAM that is available. Smaller areas such as Baden-Württemberg in Germany can run happily on a mid range system, but for larger areas you need more RAM. For example, in the openrouteservice infrastructure, each profile for the planet is running on a 128GB machine. Anything less than that and you will run out of memory during the build process.

The main things that affect the amount of RAM needed are:
* The size of the data being loaded (OSM data)
* The profile being built (bike and pedestrian need more space than driving profiles)
* The routing optimisations (having the optimised routing algorithms needs more RAM)
* Elevation (using elevation needs the elevation data to also be loaded into RAM, so the larger the geographic area, the more RAM is needed for elevation)
* Extra info (Each profile can have extra info calculated, each of these requires RAM for storage).

As a guide, you can look at the size of OSM data extracts as a rough guide as to how much RAM you would need. [Geofabrik](https://download.geofabrik.de) provides a number of these extracts along with the file sizes of the pbf files. The planet file is around 50GB and for bike and pedestrian profiles, this needs around 100-105GB RAM. Germany is around 3GB and can normally be built on a reasonable system with around 8GB RAM, and Baden-Württemberg is 450MB which can be built on a machine with around 2-4GB RAM. In general though, having more RAM also speeds up the build process as less garbage collection actions need to be taken, and you should also be aware of any other services running outside of openrouteservice that consume RAM on the machine.

 
