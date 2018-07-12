#!/bin/bash
#
#mvn clean install package -DskipTests=true
#cf t
#echo -n "Validate the space & org, you are currently logged in before continuing!"
#read
#cf cs p-identity auth sso
#cf cs p.mysql db-small bckservice-db
##cf cs postgres standard menu-service-db
##cf cs postgres standard order-service-db
#cf cs p-rabbitmq standard event-bus
#cf cs p-config-server standard config-server -c '{"git":{"privateKey":"-----BEGIN RSA PRIVATE KEY-----\nExample\n-----END RSA PRIVATE KEY-----\n","uri":"git@github.com:jigsheth57/config-repo.git"}}'
#cf cs p-service-registry standard service-registry
#cf cs p-circuit-breaker-dashboard standard circuit-breaker
#echo "Checking status of the Service Instances!"
#until [ `cf service bckservice-db | grep -c "succeeded"` -eq 1  ]

#  echo -n "."
#  sleep 5s
#done
#until [ `cf service config-server | grep -c "succeeded"` -eq 1  ]
#do
#  echo -n "."
#  sleep 5s
#done
#until [ `cf service service-registry | grep -c "succeeded"` -eq 1  ]
#do
#  echo -n "."
#  sleep 5s
#done
#until [ `cf service circuit-breaker | grep -c "succeeded"` -eq 1  ]
#do
#  echo -n "."
#  sleep 5s
#done
#echo
#echo "Service instances created. Pushing all required applications."
#
#cf p

cf add-network-policy mds-admin-portal --destination-app mds-menu-service --protocol tcp --port 8443
cf add-network-policy mds-admin-portal --destination-app mds-order-service --protocol tcp --port 8443
cf add-network-policy mds-customer-portal --destination-app mds-menu-service --protocol tcp --port 8443
cf add-network-policy mds-customer-portal --destination-app mds-order-service --protocol tcp --port 8443
cf add-network-policy mds-order-service --destination-app mds-menu-service --protocol tcp --port 8443
