#!/bin/bash

ssh -n -i /home/rgrundy/rgrundy-keypair.pem $1 "git clone git@github.com:bowdoin-dsys/p4-final-r-2.git"

