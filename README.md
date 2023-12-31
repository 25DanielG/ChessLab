#### A minimax chess player w/ 
 - Move ordering: MVVLVA, History heuristics
 - Alpha-beta pruning
 - Null Move pruning
 - Futility Sruning
 - Quiescence Search
 - Iterative Deepening
 - Aspiration windows
 - Multi-principal variations
 - Multithreaded
 - Fast bitboard

#### Beginning Game
 - Uses an opening book engine w/ ~2k theoretical openings and lines

#### End Game
 - Tablebase

#### Scoring function based on
 - Piece value
 - Pawn structure
 - Piece mobility
 - Piece development
 - King safety
 - Rook's open files
 - Center control
 - Bishop pairs
 - Outpost knights

##### Dependencies
Maven
JDK 17
##### Running
mvn compile
mvn clean install
mvn exec:java
##### Extra information
Runs Depth _ in a stable time limit
DataSets: https://www.kaggle.com/datasets/ronakbadhe/chess-evaluations