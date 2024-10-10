# Anthill: A Decentralized Peer-to-Peer System for Anonymizing Network Traffic

**Distributed Systems 3325 Final Project**  
Contributors: Ryan Novitski, Ross Grundy

---

## Overview
Anthill is a decentralized distributed system designed to anonymize application-layer network traffic when sending HTTP requests. Built with a peer-to-peer (P2P) ring architecture, the system allows for secure and anonymous transmission of data. It leverages Java, XML-RPC, and the Apache HTTP library to ensure scalable, fault-tolerant, and anonymous communication.

## Features
- **Anonymized HTTP Requests**: Secure sending and receiving of HTTP requests without traceable origins.
- **Peer-to-Peer Ring Architecture**: Ensures no single point of failure and dynamic fault tolerance.
- **Dynamic Node Management**: Nodes can be dynamically added or removed without interrupting the network.
- **Colony Table**: Efficient path traversal and fault tolerance via exponential node storage.
- **Request Randomization**: Requests are routed randomly through multiple nodes, providing a high level of anonymity.

## Technologies Used
- **Languages**: Java, Bash
- **Libraries/Frameworks**: Apache XML-RPC, Apache HTTP Library
- **Additional Tools**: Bash scripting for server setup and node management

## Architecture
Anthill operates on a peer-to-peer (P2P) ring structure where nodes, referred to as "drones," are connected in a decentralized manner. Each drone holds a partial list of other nodes (a "colony table") to facilitate communication. The system uses a dynamic algorithm to propagate changes when nodes are added or removed, ensuring resilience and scalability.

Key Components:
- **Drones**: Each node operates independently, can send/receive HTTP requests, and manages its own colony table.
- **Colony Table**: Each node stores exponential successors (2, 4, 8) in its table, enabling efficient traversal across the ring.
- **Randomized Request Paths**: Ensures requests are anonymized by traversing multiple nodes before execution.

## Setup and Installation

### Prerequisites
- Java 1.8
- Apache XML-RPC Library
- Bash (for node setup)

### Steps to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/Anthill.git
   cd Anthill
2. Compile the Java code:
   ```bash
   make
4. Start the bootstrap node:
   ```bash
   java LogServer [bootstrap-node-ip]
5. Connect additional nodes:
   ```bash
   java LogBot [bootstrap-node-ip]
## Usage
Once nodes are set up, you can send HTTP requests via the command line by interacting with any of the drones in the system.

### Commands

Send: Send an HTTP request through the network.
Info: View the current colony table for a node.
Quit: Exit the network.
Performance and Evaluation

Latency: As the path length increases, latency grows exponentially, making paths longer than 10 nodes impractical for fast transfers.
Fault Tolerance: The system maintains communication even as nodes join or leave, dynamically updating colony tables for stability.
Future Improvements

Encryption: Integration of onion-routing and public-key encryption for added security.
Garlic Routing: Concealing the quantity of requests by clumping them together.

## Contributors

- [Ryan Novitski](https://github.com/rnovitski24)
- [Ross Grundy](https://github.com/rgrundy202)
