# organization-rest-service
This service maintains the organization, the associated users and their associated positions.


## Run
Run locally with the Eureka profile, which picks up properties defined in `application-eureka.yaml`:

```
SPRING_PROFILES_ACTIVE=eureka ./gradlew bootRun
```

For local HTTPS:

```
SPRING_PROFILES_ACTIVE=eureka,local-https ./gradlew bootRun
```

## Build Docker image
Gradle build:
```
./gradlew bootBuildImage --imageName=name/organization-rest-service
```
Docker build passing in username and personal access token varaibles into docker to be used as environment variables in the gradle `build.gradle` file for pulling private maven artifact:
```
docker build --secret id=USERNAME,env=USERNAME --secret id=PERSONAL_ACCESS_TOKEN,env=PERSONAL_ACCESS_TOKEN . -t my/organization-rest-service
```

Pass the Eureka profile as argument:
```
docker run -e SPRING_PROFILES_ACTIVE=eureka -p 8088:8088 -t myorg/organization-rest-service
```

or pass the environment variables for database information:

`docker run -e POSTGRES_USERNAME=dummy \
 -e POSTGRES_PASSWORD=dummy -e POSTGRES_DBNAME=account \
  -e POSTGRES_SERVICE=localhost:5432 \
 --publish 8080:8080 imageregistry/organization-rest-service:1.0`


## Installation on Kubernetes
Use my Helm chart here @ [sonam-helm-chart](https://github.com/sonamsamdupkhangsar/sonam-helm-chart):

```
helm install project-api sonam/mychart -f values.yaml --version 0.1.12 --namespace=yournamespace
```

##Instruction for port-forwarding database pod
```
export PGMASTER=$(kubectl get pods -o jsonpath={.items..metadata.name} -l application=spilo,cluster-name=project-minimal-cluster,spilo-role=master -n yournamesapce); 
echo $PGMASTER;
kubectl port-forward $PGMASTER 6432:5432 -n backend;
```

###Login to database instruction
```
export PGPASSWORD=$(kubectl get secret <SECRET_NAME> -o 'jsonpath={.data.password}' -n backend | base64 -d);
echo $PGPASSWORD;
export PGSSLMODE=require;
psql -U <USER> -d projectdb -h localhost -p 6432

```
