#!/bin/bash

BOOTSTRAP_IP=$1
ip_address=$2

git pull
    make

    if [ -z "$BOOTSTRAP_IP" ]; then # First IP in list is initializer
        BOOTSTRAP_IP=$ip_address
        java anthill.Drone --initialize # runs initializeNetwork()
        

    else 
        # if bootstrap is already set, join network
        java anthill.Drone --join $BOOTSTRAP_IP # runs joinNetwork(bootstrapIP)
        
    fi
done
