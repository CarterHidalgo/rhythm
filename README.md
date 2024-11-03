# Rhythm Chess Engine

UCI complient Java chess engine. This is a modification/re-working of my prior non-UCI Java/JavaFX chess engine (see https://github.com/CarterHidalgo/old-javafx-chess-engine.git). 

## Features

**Bitboards**: Uses 64-bit integers to store piece location as well as do binary operations for improved speed

**Magic Bitboards**: Uses pre-computed sliding piece attack sets with a shifted compacting scheme for better memory use and faster move generation

**Helpful CLI**: Contains helpful CLI commands such as ```print``` to better manipulate the engine when running headless in the terminal

## Other

**Gradle**: ./gradlew createExe

## Contributions 

Contributions are welcome! I update this repo somewhat unfrequently, but do not be fooled. This is an active project that I work on almost every day.
