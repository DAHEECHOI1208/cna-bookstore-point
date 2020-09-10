# cna-bookstore(revision_version)

## User Scenario
```
* 고객 또는 고객 운영자는 고객정보를 생성한다.
* 북 관리자는 판매하는 책의 종류와 보유 수량을 생성하고 수정할 수 있다.
* 고객은 책의 주문과 취소가 가능하며 주문 정보의 수정은 없다.
* 고객이 주문을 생성할 때 고객정보와 Book 정보와 Point 정보가 있어야 한다.(revision_version)
  - Order -> Customer 동기호출
  - Order -> Point 동기호출
  - Order -> BookInventory 동기호출
  - Customer 서비스가 중지되어 있더라도 주문은 생성하되 주문상태를 "Customer_Not_Verified"로 설정하여 타 서비스로의 전달은 진행하지 않는다.
  - Point 서비스가 중지되어 있더라도 주문은 생성하되 주문상태를 "Point_Not_Verified"로 설정하여 타 서비스로의 전달은 진행하지 않는다.
* 주문 시에 재고가 없더라도 주문이 가능하다.
  - 주문 상태는 “Ordered”
* 주문 취소는 "Ordered" 상태일 경우만 가능하다.
* 주문 시에 Point 정보를 참조하여 Discount 가능하다.(revision_version)
* 배송준비는 주문 정보를 받아 이뤄지며 재고가 부족한 경우, 책 입고가 이뤄져서 재고 수량이 충분한 경우에 배송 준비가 완료되었음을 알린다.
* 배송은 주문 생성 정보를 받아서 배송을 준비하고 주문 상품 준비 정보를 받아서 배송을 시작하며 배송이 시작되었음을 주문에도 알린다.
  - 주문 생성 시 배송 생성
  - 상품 준비 시 배송 시작  
* 배송을 시행하는 외부 시스템(물류 회사 시스템) 또는 배송 담당자는 배송 단계별로 상태는 변경한다. 변경된 배송 상태는 주문에 알려 반영한다.
* 주문 취소되더라도 고객은 MyPage에서 주문 이력을 모두 조회할 수 있다.
```
## 아키텍처
```
* 모든 요청은 단일 접점을 통해 이뤄진다.

```

## Cloud Native Application Model(revision_version)
![Alt text](msaez.io.PNG?raw=true "Optional Title")

## 구현 점검

### 모든 서비스 정상 기동 
```
* Httpie Pod 접속
kubectl exec -it httpie -- bash

* API 
http http://gateway:8080/customers
http http://gateway:8080/myPages
http http://gateway:8080/books
http http://gateway:8080/deliverables
http http://gateway:8080/stockInputs
http http://gateway:8080/orders
http http://gateway:8080/deliveries
http http://gateway:8080/points(revision_version)
```

![Alt text](getPoints.PNG?raw=true "Optional Title")

### Kafka 기동 및 모니터링 용 Consumer 연결
```
kubectl -n kafka exec -ti my-kafka-0 -- /usr/bin/kafka-console-consumer --bootstrap-server my-kafka:9092 --topic cnabookstore --from-beginning
```
![Alt text](getKafka.PNG?raw=true "Optional Title")

### 고객 생성
```
http POST http://gateway:8080/customers customerName="CDH"
http POST http://gateway:8080/customers customerName="KJW"
```
![Alt text](getCustomers.PNG?raw=true "Optional Title")

### 책 정보 생성
```
$ http POST http://gateway:8080/books bookName="Alice in a wonderland" stock=100
HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Sep 2020 01:53:45 GMT
Location: http://bookinventory:8080/books/1
transfer-encoding: chunked

{
    "_links": {
        "book": {
            "href": "http://bookinventory:8080/books/1"
        }, 
        "self": {
            "href": "http://bookinventory:8080/books/1"
        }
    }, 
    "bookName": "Alice in a wonderland", 
    "stock": 100
}

$ http POST http://gateway:8080/books bookName="Quobadis?" stock=50
HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Sep 2020 01:54:38 GMT
Location: http://bookinventory:8080/books/2
transfer-encoding: chunked

{
    "_links": {
        "book": {
            "href": "http://bookinventory:8080/books/2"
        }, 
        "self": {
            "href": "http://bookinventory:8080/books/2"
        }
    }, 
    "bookName": "Quobadis?", 
    "stock": 50
}

```
![Alt text](getBooks.PNG?raw=true "Optional Title")

### 주문 생성
```
http POST http://gateway:8080/orders bookId=1 customerId=1 deliveryAddress="Bundang" quantity=50
http POST http://gateway:8080/orders bookId=1 customerId=2 deliveryAddress="Seoul" quantity=100
```
![Alt text](getOrders.PNG?raw=true "Optional Title")

##### Message 전송 확인 결과
```
{"eventType":"Ordered","timestamp":"20200909024119","orderId":4,"bookId":1,"customerId":2,"quantity":100,"deliveryAddress":"Seoul","orderStatus":"ORDERED","me":true}
```
![Alt text](getKafka2.PNG?raw=true "Optional Title")

### 포인트 정보 생성(revision)

http POST http://gateway:8080/points pointId=1 pointValue=10000
http POST http://gateway:8080/points pointId=2 pointValue=20000

![Alt text](getPoints2.PNG?raw=true "Optional Title")

##### Deliveriy 확인 결과
```
root@httpie:/# http http://gateway:8080/deliveraries
{
    "_links": {
        "delivery": {
            "href": "http://delivery:8080/deliveries/4"
        }, 
        "self": {
            "href": "http://delivery:8080/deliveries/4"
        }
    }, 
    "deliveryAddress": "Seoul", 
    "deliveryStatus": "CreateDelivery", 
    "orderId": 4
}

```

![Alt text](getDeliveries.PNG?raw=true "Optional Title")

##### Deliverables 확인 결과
```
root@httpie:/# http http://gateway:8080/deliverables
           {
                "_links": {
                    "deliverable": {
                        "href": "http://bookinventory:8080/deliverables/5"
                    }, 
                    "self": {
                        "href": "http://bookinventory:8080/deliverables/5"
                    }
                }, 
                "orderId": 4, 
                "quantity": 100, 
                "status": "Stock_Lacked"
            }
```
![Alt text](getDeliverables.PNG?raw=true "Optional Title")

### 주문 준비
```
root@httpie:/# http POST http://gateway:8080/stockInputs bookId=1 quantity=200
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Sep 2020 02:47:04 GMT
transfer-encoding: chunked

{
    "bookId": 1, 
    "id": 8, 
    "inCharger": null, 
    "quantity": 200
}
```
```
{"eventType":"DeliveryPrepared","timestamp":"20200909024704","id":null,"orderId":4,"status":"Delivery_Prepared","me":true}
{"eventType":"DeliveryStatusChanged","timestamp":"20200909024704","id":4,"orderId":4,"deliveryStatus":"Shipped","me":true}
```
![Alt text](geStock.PNG?raw=true "Optional Title")
![Alt text](geStock2.PNG?raw=true "Optional Title")

##### 재고 수량 변경 확인 결과
```
root@httpie:/# http http://gateway:8080/books/1 
HTTP/1.1 200 OK
Content-Type: application/hal+json;charset=UTF-8
Date: Wed, 09 Sep 2020 02:53:26 GMT
transfer-encoding: chunked
{
    "_links": {
        "book": {
            "href": "http://bookinventory:8080/books/1"
        }, 
        "self": {
            "href": "http://bookinventory:8080/books/1"
        }
    }, 
    "bookName": "Alice in a wonderland", 
    "stock": 150
}
```
![Alt text](geStock3.PNG?raw=true "Optional Title")

##### 주문 상태 변경 확인 결과
```
root@httpie:/# http http://gateway:8080/orders/2
HTTP/1.1 200 OK
Content-Type: application/hal+json;charset=UTF-8
Date: Wed, 09 Sep 2020 02:55:30 GMT
transfer-encoding: chunked

{
    "_links": {
        "order": {
            "href": "http://order:8080/orders/2"
        }, 
        "self": {
            "href": "http://order:8080/orders/2"
        }
    }, 
    "bookId": 1, 
    "customerId": 2, 
    "deliveryAddress": "Seoul", 
    "orderStatus": "Shipped", 
    "quantity": 100
}

![Alt text](geOrderStatus.PNG?raw=true "Optional Title")

root@httpie:/# http http://gateway:8080/deliverables/
HTTP/1.1 200 OK
Content-Type: application/hal+json;charset=UTF-8
Date: Wed, 09 Sep 2020 02:55:07 GMT
transfer-encoding: chunked

{
    "_links": {
        "deliverable": {
            "href": "http://bookinventory:8080/deliverables/4"
        }, 
        "self": {
            "href": "http://bookinventory:8080/deliverables/4
        }
    }, 
    "orderId": 3, 
    "quantity": 100, 
    "status": "Delivery_Prepared"
}
```
![Alt text](geDeliverables2.PNG?raw=true "Optional Title")

### 포인트 조회 및 주문 생성


### 배송 상태 변경
```
```

### 고객 Mypage 이력 확인
```
```

### 장애 격리
```
1. Customer 서비스 중지
	kubectl delete deploy customer
	
2. 주문 생성
	root@httpie:/# http POST http://gateway:8080/orders bookId=1 customerId=1 deliveryAddress="Bundang" quantity=50

3. 주문 생성 결과 확인
HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Sep 2020 02:00:35 GMT
Location: http://order:8080/orders/1
transfer-encoding: chunked

{
    "_links": {
        "order": {
            "href": "http://order:8080/orders/1"
        }, 
        "self": {
            "href": "http://order:8080/orders/1"
        }
    }, 
    "bookId": 1, 
    "customerId": 1, 
    "deliveryAddress": "Bundang", 
    "orderStatus": "Customer_Not_Verified", 
    "quantity": 50
}
```

1. Point 서비스 중지(revision)
	kubectl delete deploy point
	
2. 포인트 조회
	root@httpie:/# http POST http://gateway:8080/orders customerId=1 customerName="CDH" pointId=1

3. 포인트 조회 결과 확인

![Alt text](pointNotVerified.PNG?raw=true "Optional Title")

## CI/CD 점검
![Alt text](azureCI.PNG?raw=true "Optional Title")
![Alt text](azureCD.PNG?raw=true "Optional Title")

## Circuit Breaker 점검

```
Hystrix Command
	5000ms 이상 Timeout 발생 시 CircuitBearker 발동

CircuitBeaker 발생
	http http://delivery:8080/selectDeliveryInfo?deliveryId=1
		- 잘못된 쿼리 수행 시 CircuitBeaker
		- 10000ms(10sec) Sleep 수행
		- 5000ms Timeout으로 CircuitBeaker 발동
		- 10000ms(10sec) 
```

```
실행 결과

root@httpie:/# http http://delivery:8080/selectDeliveryInfo?deliveryId=1
HTTP/1.1 200 
Content-Length: 7
Content-Type: text/plain;charset=UTF-8
Date: Wed, 09 Sep 2020 04:27:53 GMT

Shipped

root@httpie:/# http http://delivery:8080/selectDeliveryInfo?deliveryId=0
HTTP/1.1 200 
Content-Length: 17
Content-Type: text/plain;charset=UTF-8
Date: Wed, 09 Sep 2020 04:28:03 GMT

CircuitBreaker!!!

root@httpie:/# http http://delivery:8080/selectDeliveryInfo?deliveryId=1
HTTP/1.1 200 
Content-Length: 17
Content-Type: text/plain;charset=UTF-8
Date: Wed, 09 Sep 2020 04:28:06 GMT

CircuitBreaker!!!

```

```
소스 코드

@GetMapping("/selectDeliveryInfo")
  @HystrixCommand(fallbackMethod = "fallbackDelivery", commandProperties = {
          @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000"),
          @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000")
  })
  public String selectDeliveryInfo(@RequestParam long deliveryId) throws InterruptedException {

   if (deliveryId <= 0) {
    System.out.println("@@@ CircuitBreaker!!!");
    Thread.sleep(10000);
    //throw new RuntimeException("CircuitBreaker!!!");
   } else {
    Optional<Delivery> delivery = deliveryRepository.findById(deliveryId);
    return delivery.get().getDeliveryStatus();
   }

   System.out.println("$$$ SUCCESS!!!");
   return " SUCCESS!!!";
  }

 private String fallbackDelivery(long deliveryId) {
  System.out.println("### fallback!!!");
  return "CircuitBreaker!!!";
 }
```

## Autoscale 점검(revision_version)
### 설정 확인
```
deployment.yaml 파일 설정 변경
(https://k8s.io/examples/application/php-apache.yaml 파일 참고)
 resources:
  limits:
    cpu: 500m
  requests:
    cpu: 200m
```
### 점검 순서
```
1. HPA 생성 및 설정
	kubectl autoscale deploy point --min=1 --max=10 --cpu-percent=30
	kubectl get hpa point -o yaml
2. 모니터링 걸어놓고 확인
	kubectl get hpa point -w
	watch kubectl get deploy,po
3. Siege 실행
  siege -c10 -t60S -v http://gateway:8080/points/
  ```
  
### 점검 결과
![Alt text](autoscale1.PNG?raw=true "Optional Title")
![Alt text](autoscale2.PNG?raw=true "Optional Title")
![Alt text](autoscale3.PNG?raw=true "Optional Title")

## Readiness Probe 점검
### 설정 확인
```
readinessProbe:
  httpGet:
    path: '/points'
    port: 8080
  initialDelaySeconds: 12
  timeoutSeconds: 2
  periodSeconds: 5
  failureThreshold: 10
  ```
### 점검 순서

#### 1. Readiness 설정 제거 후 배포

#### 2. Siege 실행
```
siege -c2 -t60S -v http://gateway:8080/points
```

#### 3. Siege 결과 Availability 확인(100% 미만)
![Alt text](readinessN.PNG?raw=true "Optional Title")

#### 4. Readiness 설정 추가 후 재배포

#### 5. Siege 실행
```
siege -c2 -t120S -v http://gateway:8080/points
```
#### 6. Siege 결과 Availability 확인(100%)

![Alt text](readinessY.PNG?raw=true "Optional Title")


## Liveness Probe 점검
### 설정 확인
```
livenessProbe:
  httpGet:
    path: '/isHealthy'
    port: 8080
  initialDelaySeconds: 120
  timeoutSeconds: 2
  periodSeconds: 5
  failureThreshold: 5
```
### 점검 순서 및 결과
#### 1. 기동 확인
```
http http://gateway:8080/orders
```
#### 2. 상태 확인
```
oot@httpie:/# http http://order:8080/isHealthy
HTTP/1.1 200 
Content-Length: 0
Date: Wed, 09 Sep 2020 02:14:22 GMT
```

#### 3. 상태 변경
```
root@httpie:/# http http://order:8080/makeZombie
HTTP/1.1 200 
Content-Length: 0
Date: Wed, 09 Sep 2020 02:14:24 GMT
```
#### 4. 상태 확인
```
root@httpie:/# http http://order:8080/isHealthy
HTTP/1.1 500 
Connection: close
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Sep 2020 02:14:28 GMT
Transfer-Encoding: chunked

{
    "error": "Internal Server Error", 
    "message": "zombie.....", 
    "path": "/isHealthy", 
    "status": 500, 
    "timestamp": "2020-09-09T02:14:28.338+0000"
}
```
#### 5. Pod 재기동 확인
```
root@httpie:/# http http://order:8080/isHealthy
http: error: ConnectionError: HTTPConnectionPool(host='order', port=8080): Max retries exceeded with url: /makeZombie (Caused by NewConnectionError('<requests.packages.urllib3.connection.HTTPConnection object at 0x7f5196111c50>: Failed to establish a new connection: [Errno 111] Connection refused',))

root@httpie:/# http http://order:8080/isHealthy
HTTP/1.1 200 
Content-Length: 0
Date: Wed, 09 Sep 2020 02:36:00 GMT
```
## ConfigMap 설정(Semi by TestCode)(revisioning)

kubectl create configmap hello-cm --from-literal=language=java
kubectl get cm
kubectl get cm hello-cm -o yaml
az acr build --registry admin16acr --image admin16acr.azurecr.io/cm-sandbox:v1 .
nano cm-deployment.yaml
![Alt text](configmap2.PNG?raw=true "Optional Title")

### 배포 및 서비스 생성
kubectl create -f cm-deployment.yaml
kubectl create -f cm-service.yaml
![Alt text](configmap1.PNG?raw=true "Optional Title")

### 서비스 확인
Service의 External-IP 접속
![Alt text](configmap3.PNG?raw=true "Optional Title")
