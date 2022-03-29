from http import HTTPStatus
from threading import Thread
import requests

# Scenario:
#  Check if the status of an order is "unassigned"
#  it cannot successfully pass through restaurant and wallet service.

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


    # Customer 301 makes an order for total amount.
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 101,
        "itemId" : 2,
        "qty" : 10
    }) 
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return "Fail1"

    # Check if the initial orderId is 1000
    orderId = http_response.json().get("orderId")

    if orderId != 1000:
        return "Fail2"

    # Check the status of Order Id 1000
    http_response = requests.get(
        f"http://localhost:8081/order/"+str(orderId))

    status = http_response.json().get("status")

    # Check if status is unassigned
    if status != "unassigned":
        return "Fail3"  
    

    return "Pass"

if __name__ == "__main__":
    print(test())