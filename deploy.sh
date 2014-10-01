#!/bin/bash
cd /home/carbondbui/app
su carbondbui -c "git pull"
su carbondbui -c "/home/carbondbui/app/activator clean stage"
supervisorctl restart carbondbui
