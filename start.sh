#!/bin/bash

cd server ; ./start.sh &
cd ../client-react ; npm start &
