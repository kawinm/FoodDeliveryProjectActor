from http import HTTPStatus
from threading import Thread
import requests

# Scenario:
#  Check if the status code for the reinitialization request 
#  is 201.

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082


def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")

    # Customer 301 makes an order for total amount 230.
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 101,
        "itemId" : 2,
        "qty" : 1
    }) 
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return "Fail1"

    # Check if the initial orderId is 1000
    orderId = http_response.json().get("orderId")

    if orderId != 1000:
        return "Fail2"

    # Customer 301 makes an order for total amount 230.
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 101,
        "itemId" : 2,
        "qty" : 1
    }) 
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return "Fail3"

    # Check if the subsequent orderId is 1001
    orderId = http_response.json().get("orderId")

    if orderId != 1001:
        return "Fail4"

    # Customer 301 makes an order for total amount 230.
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 101,
        "itemId" : 2,
        "qty" : 1
    }) 
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return "Fail5"

    # Check if the subsequent orderId is 1002
    orderId = http_response.json().get("orderId")

    if orderId != 1002:
        return "Fail6"

    # Reinitialize the Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return "Fail7"


    return "Pass"

if __name__ == "__main__":
    print(test())