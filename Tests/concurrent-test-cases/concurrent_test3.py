from http import HTTPStatus
from threading import Thread
import requests

# Scenario:
# Stress testing of delivery service to check whether 
# the AutoScalar is scaling up the number of pods of delivery service

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082

MAXIMUM_REQUESTS = 100

def t1(result):  # First concurrent request

    # Refills the item 1 from restaurant 101 by quantity 1
    http_response = requests.post(
        "http://localhost:8080/refillItem", json={"restId": 101, "itemId":1, "qty":1})

    result["1"] = http_response


def t2(result):  # Second concurrent request

    # Requests an order for item 1 from restaurant 1 for quantity 1
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 1})

    result["2"] = http_response

def t3(result):  # Third concurrent request

    # Add amount 400 to wallet of Customer 301
    http_response = requests.post(
        "http://localhost:8082/addBalance", json={"custId": 301, "amount":400})

    result["1"] = http_response

def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")

    ### Parallel Execution Begins ###

    try:
        thread = [0 for i in range(3*MAXIMUM_REQUESTS)]
        for i in range(MAXIMUM_REQUESTS):
            thread[3*i] = Thread(target=t1, kwargs={"result": result})
            thread[(3*i)+1] = Thread(target=t2, kwargs={"result": result})
            thread[(3*i)+2] = Thread(target=t3, kwargs={"result": result})

        for i in range(MAXIMUM_REQUESTS):
            thread[3*i].start()
            thread[(3*i)+1].start()
            thread[(3*i)+2].start()

        for i in range(MAXIMUM_REQUESTS):
            thread[3*i].join()
            thread[(3*i)+1].join()
            thread[(3*i)+2].join()
    except:
        return "Fail2"

    ### Parallel Execution Ends ###

    
    return 'Pass'


if __name__ == "__main__":

    print(test())