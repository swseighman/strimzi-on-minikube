Installing and using the Strimzi Operator with minikube

Install minikube
Make certain you have kubectl installed. You can install kubectl according to the instructions in Install and Set Up kubectl.

If you do not already have a hypervisor installed, install one of these now:
• KVM, which also uses QEMU
• VirtualBox
 
Download the latest version of minikube
https://github.com/kubernetes/minikube/releases
 
$ sudo dnf install <path to>minikube-1.9.2-0.x86_64.rpm
 
or
 
$ curl -L0 minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 \
&& chmod +x minikube

$ sudo mkdir -p /usr/local/bin/
$ sudo install minikube /usr/local/bin/

$ minikube version
minikube version: v1.9.2
commit: 93af9c1e43cab9618e301bc9fa720c63d5efa393

See https://kubernetes.io/docs/tasks/tools/install-minikube/ for additional info.


Start minikube
You can choose the driver, options are virtualbox, kvm, and podman (experimental).
$  minikube start --driver=virtualbox --kubernetes-version=1.18.0
😄  minikube v1.9.2 on Fedora 31
✨  Using the virtualbox driver based on user configuration
💿  Downloading VM boot image ...
    > minikube-v1.9.0.iso.sha256: 65 B / 65 B [--------------] 100.00% ? p/s 0s
    > minikube-v1.9.0.iso: 174.93 MiB / 174.93 MiB [-] 100.00% 9.20 MiB p/s 20s
👍  Starting control plane node m01 in cluster minikube
💾  Downloading Kubernetes v1.18.0 preload ...
    > preloaded-images-k8s-v2-v1.18.0-docker-overlay2-amd64.tar.lz4: 542.91 MiB
🔥  Creating virtualbox VM (CPUs=2, Memory=5900MB, Disk=20000MB) ...
🐳  Preparing Kubernetes v1.18.0 on Docker 19.03.8 ...
🌟  Enabling addons: default-storageclass, storage-provisioner
🏄  Done! kubectl is now configured to use "minikube"


$ minikube status
m01
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured

Install the Strimzi Operator
$ curl -L0 https://strimzi.io/install/latest
$ curl -L0 https://strimzi.io/install/latest | sed 's/namespace: .*/namespace: default/' | kubectl apply -f -
$ kubectl get pods
NAME                                        READY   STATUS    RESTARTS   AGE
strimzi-cluster-operator-6c8d574d49-hch89   1/1     Running   0          91s

Create the Kafka Cluster
$ curl -L0 https://strimzi.io/examples/latest/kafka/kafka-persistent.yaml | vi -
apiVersion: kafka.strimzi.io/v1beta1
kind: Kafka
metadata:
  name: my-cluster
spec:
  kafka:
    version: 2.4.0
    replicas: 3
    listeners:
      plain: {}
      tls: {}
      external:
        type: loadbalancer
        tls: false
    config:
      offsets.topic.replication.factor: 3
      transaction.state.log.replication.factor: 3
      transaction.state.log.min.isr: 2
      log.message.format.version: "2.4"
    storage:
      type: jbod
      volumes:
      - id: 0
        type: persistent-claim
        size: 100Gi
        deleteClaim: false
  zookeeper:
    replicas: 3
    storage:
      type: persistent-claim
      size: 100Gi
      deleteClaim: false
  entityOperator:
    topicOperator: {}
    userOperator: {}
:w ! kubectl apply -f -
kafka.kafka.strimzi.io/my-cluster created
Use ‘minikube tunnel’ to expose external IP
minikube tunnel runs as a process, creating a network route on the host to the service CIDR of the cluster using the cluster’s IP address as a gateway. The tunnel command exposes the external IP directly to any program running on the host operating system.  
In another terminal, run:
$ minikube tunnel 
Status:	
	machine: minikube
	pid: 112455
	route: 10.96.0.0/12 -> 192.168.99.102
	minikube: Running
	services: [my-cluster-kafka-0, my-cluster-kafka-1, my-cluster-kafka-2, my-cluster-kafka-external-bootstrap]
    errors: 
		minikube: no errors
		router: no errors
		loadbalancer emulator: no errors

$ kubectl get pods
NAME                                          READY   STATUS    RESTARTS   AGE
my-cluster-entity-operator-5dd6d7f5bd-c76b4   3/3     Running   0          88m
my-cluster-kafka-0                            2/2     Running   0          88m
my-cluster-kafka-1                            2/2     Running   0          88m
my-cluster-kafka-2                            2/2     Running   0          88m
my-cluster-zookeeper-0                        2/2     Running   0          90m
my-cluster-zookeeper-1                        2/2     Running   0          90m
my-cluster-zookeeper-2                        2/2     Running   0          90m
strimzi-cluster-operator-6c8d574d49-hch89     1/1     Running   0          107m

$ kubectl get kafka -o yaml
apiVersion: v1
items:
- apiVersion: kafka.strimzi.io/v1beta1
  kind: Kafka
  metadata:
    annotations:
      kubectl.kubernetes.io/last-applied-configuration: |
        {"apiVersion":"kafka.strimzi.io/v1beta1","kind":"Kafka","metadata":{"annotations":{},"name":"my-cluster","namespace":"default"},"spec":{"entityOperator":{"topicOperator":{},"userOperator":{}},"kafka":{"config":{"log.message.format.version":"2.4","offsets.topic.replication.factor":3,"transaction.state.log.min.isr":2,"transaction.state.log.replication.factor":3},"listeners":{"external":{"tls":false,"type":"loadbalancer"},"plain":{},"tls":{}},"replicas":3,"storage":{"type":"jbod","volumes":[{"deleteClaim":false,"id":0,"size":"100Gi","type":"persistent-claim"}]},"version":"2.4.0"},"zookeeper":{"replicas":3,"storage":{"deleteClaim":false,"size":"100Gi","type":"persistent-claim"}}}}
    creationTimestamp: "2020-04-15T14:40:27Z"
    generation: 1
    managedFields:
    - apiVersion: kafka.strimzi.io/v1beta1
      fieldsType: FieldsV1
      fieldsV1:
        f:spec:
          f:entityOperator:
            f:topicOperator:
              f:reconciliationIntervalSeconds: {}
              f:topicMetadataMaxAttempts: {}
              f:zookeeperSessionTimeoutSeconds: {}
            f:userOperator:
              f:reconciliationIntervalSeconds: {}
              f:zookeeperSessionTimeoutSeconds: {}
        f:status:
          f:conditions: {}
          f:listeners: {}
          f:observedGeneration: {}
      manager: okhttp
      operation: Update
      time: "2020-04-15T14:43:17Z"
    name: my-cluster
    namespace: default
    resourceVersion: "4802"
    selfLink: /apis/kafka.strimzi.io/v1beta1/namespaces/default/kafkas/my-cluster
    uid: 55993681-f909-40ad-ba4c-b94968aba11f
  spec:
    entityOperator:
      topicOperator: {}
      userOperator: {}
    kafka:
      config:
        log.message.format.version: "2.4"
        offsets.topic.replication.factor: 3
        transaction.state.log.min.isr: 2
        transaction.state.log.replication.factor: 3
      listeners:
        external:
          tls: false
          type: loadbalancer
        plain: {}
        tls: {}
      replicas: 3
      storage:
        type: jbod
        volumes:
        - deleteClaim: false
          id: 0
          size: 100Gi
          type: persistent-claim
      version: 2.4.0
    zookeeper:
      replicas: 3
      storage:
        deleteClaim: false
        size: 100Gi
        type: persistent-claim
  status:
    conditions:
    - lastTransitionTime: 2020-04-15T14:43:17+0000
      status: "True"
      type: Ready
    listeners:
    - addresses:
      - host: my-cluster-kafka-bootstrap.default.svc
        port: 9092
      type: plain
    - addresses:
      - host: my-cluster-kafka-bootstrap.default.svc
        port: 9093
      certificates:
      - |
        -----BEGIN CERTIFICATE-----
        MIIDLTCCAhWgAwIBAgIJAPdagdf...
        -----END CERTIFICATE-----
      type: tls
    - addresses:
      - host: 10.102.68.38    //Note the IP address
        port: 9094            //Note the port
      type: external
    observedGeneration: 1
kind: List
metadata:
  resourceVersion: ""
  selfLink: ""

Create a Kafka topic
Change the partitions and replicas to 3 for a more compelling demo.
$ curl -L0 https://strimzi.io/examples/latest/topic/kafka-topic.yaml | vi -
apiVersion: kafka.strimzi.io/v1beta1
kind: KafkaTopic
metadata:
  name: my-topic
  labels:
    strimzi.io/cluster: my-cluster
spec:
  partitions: 3
  replicas: 3
  config:
    retention.ms: 7200000
    segment.bytes: 1073741824
:w ! kubectl apply -f -


Edit and run the Producer/Consumer (see files below)
$ cd <path to source>
$ code .
Add the server IP and port from the output of the ‘kubectl get kafka -o yaml’ command to both Producer.java (line 31) and Consumer.java (line 32):
        -----END CERTIFICATE-----
      type: tls
    - addresses:
      - host: 10.102.68.38
        port: 9094
      type: external
    observedGeneration: 1
kind: List
metadata:
  resourceVersion: ""
  selfLink: ""
/*
 * Producer configuration
 */
    Map<String, Object> props = new HashMap();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.102.68.38:9094");


/*
 * Consumer configuration
 */
    Map<String, Object> props = new HashMap();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.102.68.38:9094");



Run Producer.java, notice the message was sent to partition 1, offset 1:
[main] INFO io.strimzi.demo.Producer - Message sent (topic my-topic, partition 1, offset 1)

Run Consumer.java, notice the ‘Hello World’ message was received:

[main] INFO io.strimzi.demo.Consumer - Received message: null / Hello World (from topic my-topic, partition 1, offset 1)


Stop minikube

$ minikube stop
✋  Stopping "minikube" in virtualbox ...
🛑  Node "m01" stopped.
