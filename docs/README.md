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

---

## Distributed Deployment Setup & Installation Guide

This project is designed to run as a distributed system across multiple machines or cloud instances (such as AWS EC2, which is what was used for the initial project). Below are step-by-step instructions for recreating a test environment.

### 1. Requirements

- **Multiple Machines or VMs:** Each node should run on a separate machine (can be local VMs, cloud instances like AWS EC2, etc.).
- **Java 1.8 or newer** installed on every machine.
- **Apache XML-RPC and HTTP Libraries:** Download and include in your Java classpath.
- **Networking:** Ensure all machines can communicate over the required ports (default: 8080 for HTTP/XML-RPC). Open inbound/outbound firewall rules as needed.

### 2. Setup on Each Node

1. **Clone the Repository:**
    ```bash
    git clone https://github.com/rnovitski24/Anthill-Anonymous-Network-Traffic-Distrubuted-System.git
    cd Anthill-Anonymous-Network-Traffic-Distrubuted-System
    ```

2. **Install Dependencies:**
    - Download the following JARs and place them in the project root or a `lib` directory:
        - [Apache XML-RPC](https://ws.apache.org/xmlrpc/download.html)
        - [Apache HTTP Components](https://hc.apache.org/downloads.cgi)

    - Example:  
      ```bash
      # Example for Ubuntu/Debian
      sudo apt-get install openjdk-8-jdk
      # Manually download the required JARs and add to your classpath
      ```

3. **Compile the Java Code:**
    ```bash
    make
    # or, for manual compilation:
    javac -cp ".:lib/*" *.java
    ```

### 3. Deploying the Network

#### Step 1: Start the Bootstrap Node

Pick one machine to be the bootstrap node. Note its public IP or hostname.

```bash
java -cp ".:lib/*" LogServer [bootstrap-node-ip]
```
- Replace `[bootstrap-node-ip]` with the machine’s own public IP address.

#### Step 2: Start Additional Nodes

On each additional machine, start a new node and connect it to the bootstrap node:

```bash
java -cp ".:lib/*" LogBot [bootstrap-node-ip]
```
- Replace `[bootstrap-node-ip]` with the IP address of the machine running the bootstrap node.

#### Example (AWS EC2)

1. Launch several EC2 instances (Ubuntu or Amazon Linux recommended).
2. Allow inbound traffic on port 8080 (or your chosen port) in the AWS Security Group.
3. SSH into each instance and follow the steps above.
4. Use the EC2 instance’s public IP for `[bootstrap-node-ip]` when connecting nodes.

### 4. Networking Notes

- All nodes must be able to reach each other via the network on the configured port.
- If running locally (on VMs or containers), ensure your network settings (e.g., bridged networking) allow direct communication.
- Firewalls and cloud security groups must allow inbound/outbound TCP on the required port (default: 8080).
- NAT or port forwarding may be necessary if running behind a router.

### 5. Troubleshooting

- **Nodes can’t connect:** Double-check IP addresses, firewall rules, and port configurations.
- **Java errors about missing classes:** Ensure all required JARs are present and included in your classpath.
- **Network timeouts:** Make sure the bootstrap node is running and reachable from all other machines.

---

## Usage
Once nodes are set up, you can send HTTP requests via the command line by interacting with any of the drones in the system.

### Commands

- **Send:** Send an HTTP request through the network.
- **Info:** View the current colony table for a node.
- **Quit:** Exit the network.

## Performance and Evaluation

- **Latency:** As the path length increases, latency grows exponentially, making paths longer than 10 nodes impractical for fast transfers.
- **Fault Tolerance:** The system maintains communication even as nodes join or leave, dynamically updating colony tables for stability.

## Future Improvements

- **Encryption:** Integration of onion-routing and public-key encryption for added security.
- **Garlic Routing:** Concealing the quantity of requests by clumping them together.

## Contributors

- [Ryan Novitski](https://github.com/rnovitski24)
- [Ross Grundy](https://github.com/rgrundy202)
