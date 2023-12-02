#!/bin/bash

# This bash file is called on each server to initialize or join network
# THIS IS A HELPER BASH FILE CALLED BY anthillSetup.sh

# Inputs given from anthillSetup.sh
BOOTSTRAP_IP=$1
ip_address=$2

if [ -z "$BOOTSTRAP_IP" ]; then # First IP in list is initializer
        BOOTSTRAP_IP=$ip_address
        java anthill.Drone --initialize # runs initializeNetwork()
        

    else 
        # if bootstrap is already set, join network
        java anthill.Drone --join $BOOTSTRAP_IP # runs joinNetwork(bootstrapIP)
        
fi