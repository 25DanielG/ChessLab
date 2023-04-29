#### A minimax chess player w/ 
 - Move ordering
 - Alpha-beta pruning
 - Quiescence Search (Depth 5)
 - Iterative Deepening
 - Multithreaded
 - Fast Evaluation (removes obviously bad positions before exploration)


#### Scoring function based on
 - Piece value
 - Pawn structure
 - Piece mobility
 - Piece development

##### Dependencies
Maven
JDK 17
##### Running
mvn compile
mvn clean install
mvn exec:java
##### Extra information
Runs Depth _ in a stable time limit