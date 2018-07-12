#!/bin/bash
set -ex

# vars to define
# ZONE_ENDPOINT eg freddys-bbq.login.systemdomain.com
# ZONEADMIN_CLIENT_ID 
# ZONEADMIN_CLIENT_SECRET 

ZONE_ENDPOINT=https://pademo.login.run.pivotal.io
#ZONEADMIN_CLIENT_ID=45e4bfcf-1e57-4617-99ce-3aa9c4ce6ca6
#ZONEADMIN_CLIENT_SECRET=9c9e5e10-00e6-4b9b-8f15-d2dd39bf7ed9
ZONEADMIN_CLIENT_ID=1b72c95a-8dfb-4719-b839-fe23447abca3
ZONEADMIN_CLIENT_SECRET=235b74e5-42e3-4335-9687-4c729b8102df

# uaac target $ZONE_ENDPOINT --skip-ssl-validation
uaac target $ZONE_ENDPOINT
uaac token client get $ZONEADMIN_CLIENT_ID -s $ZONEADMIN_CLIENT_SECRET
uaac user add frank --email frank@whitehouse.gov --given_name Frank --family_name Underwood -p password
uaac user add freddy --email freddy@freddysbbq.com --given_name Freddy --family_name Hayes -p password
uaac group add menu.read
uaac group add menu.write
uaac group add order.admin
uaac group add order.me
uaac member add menu.read frank
uaac member add menu.read freddy
uaac member add menu.write freddy
uaac member add order.admin freddy
uaac member add order.me frank
