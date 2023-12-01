#!/bin/bash

# Checks user and changes SSH values
while true; do
    echo "Enter username:"
    read username

    if [ "$username" = "rgrundy" ]; then
        SSH_USER="$username"
        SSH_KEY="/home/rgrundy/rgrundy-keypair.pem"
        break
    elif [ "$username" = "rnovitsk" ]; then
        SSH_USER="$username"
        SSH_KEY="/home/rnovitsk/rnovitsk-keypair.pem"
        break
    else
        echo "Invalid username. Please try again."
    fi
done

echo "Success! SSH keypair of user '$username' was set."
# TAKES IN AN IP_LIST.TXT FILE OF IP ADDRESSES TO CONNECT

IP_LIST="IPList.txt"
BOOTSTRAP_IP="" # Reserved for first node

while IFS= read -r ip_address
do

    echo "Setting up node at IP: $ip_address"

    scp -i "$SSH_KEY" ~/.ssh/$username-keypair* "$SSH_USER@$ip_address:~/.ssh/"
    scp -i "$SSH_KEY" ~/.ssh/id_rsa* "$SSH_USER@$ip_address:~/.ssh/"

<<<<<<< HEAD
    ssh -i "$SSH_KEY" "$SSH_USER@$ip_address" "./remoteSetup.sh $BOOTSTRAP_IP $ip_address"
=======
    ssh -i "$SSH_KEY" "$SSH_USER@$ip_address" "eval '$(ssh-agent -s)'&&ssh-add ~/.ssh/rgrundy-keypair.pem && git clone "git@github.com:bowdoin-dsys/p4-final-r-2.git" && cd p4-final-r-2 && git pull && ./remoteSetup.sh $BOOTSTRAP_IP $ip_address"
>>>>>>> 9dfd1c469e3e7777a3f69f5064c18f60253ef565
    
    
done < "$IP_LIST"

# Paris IP addresses
# 52.47.75.71
# 13.39.18.95

# London IP addresses
# 13.40.228.246
# 3.10.51.103

# Tokyo IP addresses
# 13.114.101.52
# 3.112.237.22