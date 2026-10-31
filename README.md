# Jellystone Forest Fire Simulation

A grid-based forest fire simulation in Java (Swing), built for COMP2000. A forest of
mixed tree species is generated, a fire is started, and it spreads tick by tick,
shaped by tree species, biomes and wind, while live statistics are shown alongside.

## Features

### Forest generation
- 40 × 60 grid; each cell has an 80% chance of holding a tree.
- Five species (Oak, Birch, Spruce, Eucalyptus, Jungle), each with its own health,
  burn intensity and spreadability, defined in the `TreeSpecies` enum.
- **Biomes:** species are chosen by a clustering algorithm that makes a tree more
  likely to match its already-planted neighbours, so same-species patches form
  naturally while the overall species mix stays balanced.

### Ignition
Each run starts the fire with one of three randomly chosen ignition sources:
- **Lightning:** strikes a random tree (shown with a strike icon).
- **Arson:** lights a specific location, searching for a flammable tree.
- **Backburning:** lights a staggered, wave-like line across a row.

### Fire spread
- Fire spreads to the 8 surrounding cells, with a chance based on each tree's
  spreadability and the wind.
- Burning trees lose health each tick and are removed when they burn out.
- Each tick's spread is decided by a **parallel stream** that only reads the forest;
  ignition is then applied afterwards on a single thread, so the parallel phase is
  thread-safe.

### Wind
- A full-strength wind blows in a random cardinal direction each run.
- Fire spreads easily with the wind and noticeably slower against it, giving the
  fire front a lopsided shape.

### Live statistics panel
Updated every tick: tick count, wind direction, total / unburnt / burning trees,
average tree health, and tree count by species. All statistics are gathered in a
single pass over the forest by a **custom stream collector** (`ForestStats`).

## Design highlights
- **Strategy pattern:** ignition behaviours (`IgnitionSource` record +
  `IgnitionBehaviour` functional interface, implemented with lambdas) and species
  selection (`SpeciesSelector`: weighted, clustering, all-oak).
- **Factory pattern:** `TreeFactory` creates trees; `IgnitionBehaviours` provides
  static factory methods for ignition strategies.
- **Generics:** `Forest<T extends Tree>` and `Cell<T extends Tree>`, with
  `Forest<? extends Tree>` for read-only consumers.
- **Streams:** filter/map/flatMap pipelines, `groupingBy`, a custom `Collector`,
  and a parallel stream for fire spread.

## Running
Run from the project root, so the `images/` folder is found:

    javac -d out src/*.java
    java -cp out Main

- `Main`: the simulation window.
- `Benchmark`: times sequential vs parallel fire spread across grid sizes.