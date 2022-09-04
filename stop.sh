#!/bin/bash

ps | egrep 'node|java' | awk '{print $1}' | xargs kill
