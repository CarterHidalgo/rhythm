# Rhythm Chess Engine

WIP UCI-complient Java chess engine. (A re-working using the core of my prior Java/JavaFX chess engine.)  

## Components

**Bitboards**: Uses 64-bit integers to store piece location as well as do binary operations for improved speed

**Magic Bitboards**: Uses pre-computed sliding piece attack sets with a shifted compacting scheme for better memory use and faster move generation

**Helpful CLI**: Contains helpful commands such as ```print``` and ```autoperft``` to better manipulate the engine when running headless in the terminal
